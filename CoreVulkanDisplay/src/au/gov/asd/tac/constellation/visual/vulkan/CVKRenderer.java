/*
 * Copyright 2010-2020 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.visual.vulkan;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKMissingEnums;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import java.awt.event.ComponentListener;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import org.lwjgl.vulkan.VkCommandBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKGlyphTextureAtlas;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKIconTextureAtlas;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKGraphLogger;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import java.awt.event.ComponentEvent;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import java.nio.LongBuffer;
import java.util.concurrent.CountDownLatch;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_DEBUGGING;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_RENDERABLE_INITIALISATION_FAILED;


/*
NAMING CONVENTION FOR CLASS MEMBERS

BLAH: static final that can be accessed from either static or instanced code
CVKBlah: a Constellation class in the au.gov.asd.tac.constellation.visual.vulkan package
cvkBlah: instance of CVKBlah class
VkBlah: Blah is LWJGL-Vulkan class, a Java object wrapping a native handle to Vulkan object
vkBlah: instance of a VkBlah class
hBlah: a handle to some object.  Treat as constant after first initialised.
pBlah: this is an object that has been explicitly malloced and needs to be explicitly released (unless
       a stack allocator was used to allocate it).
blah: anything else

FUNCTION ARGUMENTS
ommit the type preface to make it clearer to differentiate the paramter from the class variable


CODING STANDARDS
helper classes should return error codes where possible
controller classes like CVKRenderer and aggregation classes like CVKVisualProcessor will need some other mechanism
use MemoryStack to allocate nio buffers unless an allocation needs to last longer than function scope
comment where ever code is not trivially simple
make methods static whereever possible
Favour full names over abbreviations (eg Initialise() over Init()).

TODO: this is still not a good reason.  Maybe just stylistic so resource types are differentiated from other CVK types?
Resource types should expose a Create factory method and have private constructors so that we don't have to worry
about having resource types in partially initialised states.  We can also indicate failure by returning null from Create
rather than throwing exceptions from ctors.

*/

public class CVKRenderer implements ComponentListener {
    
    // We are created and displayed by this canvas
    private final CVKCanvas cvkCanvas;  
    private final CVKVisualProcessor cvkVisualProcessor;    
    private final List<CVKRenderable> renderables = new ArrayList<>();
    private final List<CVKRenderable> newRenderables = Collections.synchronizedList(new ArrayList<>());     
    private CVKSwapChain cvkSwapChain = null;   
    private CVKDescriptorPool cvkDescriptorPool = null;

    private int currentFrame = 0;
    private boolean swapChainNeedsRecreation = true;    
    private boolean descriptorPoolNeedsRecreation = true;    
    
    // We track these as they are needed to (re)create the descriptor pool
    CVKDescriptorPoolRequirements cvkDescriptorPoolRequirements = null;
    CVKDescriptorPoolRequirements cvkPerImageDescriptorPoolRequirements = null;
       
    private boolean isExiting = false;
    private final CountDownLatch exitComplete = new CountDownLatch(1);
    private float clrChange = 0.01f;
    private int curClrEl = 0;
    private Vector3f clr = new Vector3f(0.0f, 0.0f, 0.0f);
    private boolean requestScreenshot = false;
    private File requestScreenshotFile = null;
    
    public final CVKGraphLogger GetLogger() { return cvkCanvas.GetLogger(); }
    

    // ========================> Lifetime <======================== \\
    
    public CVKRenderer(CVKCanvas cvkCanvas, CVKVisualProcessor cvkVisualProcessor) {
        this.cvkCanvas = cvkCanvas;
        this.cvkVisualProcessor = cvkVisualProcessor;
    }        
    
    private void DestroyImplementation() {
        renderables.forEach(el -> {

            el.Destroy();
        });
        renderables.clear();
        
        if (cvkSwapChain != null) {
            cvkSwapChain.Destroy();
            cvkSwapChain = null;
        }
        
        if (cvkDescriptorPool != null) {
            cvkDescriptorPool.Destroy();
            cvkDescriptorPool = null;
        }
    }
        
    /**
     * Called from CVKCanvas when it's being destroyed.
     */
    public void Destroy() {      
        isExiting = true;
        GetLogger().info("CVKRenderer.destroy() called");
        if (cvkCanvas.IsRenderThreadCurrent()) {           
            CVKDevice.WaitIdle();            

            DestroyImplementation();
        } else {
            if (cvkCanvas.IsRenderThreadAlive()) {
                try {
                    GetLogger().info("\tDestroy called outside of render thread.  Waiting until it has finished destruction");   
                    exitComplete.await();
                } catch (InterruptedException e) {
                    GetLogger().severe(String.format("InterruptedException waiting for renderer to complete destruction %s", e));
                }
            } else {
                GetLogger().info("\tDestroy called outside of render thread which is no longer alive.  Attempting renderer destruction.");
                DestroyImplementation();
            }           
        }
    }  

    @SuppressWarnings("deprecation")
    @Override
    public void finalize() throws Throwable {
        Destroy();
        super.finalize();
    }    
    
    
    // ========================> Renderables <======================== \\
    
    public void AddRenderable(CVKRenderable renderable) {   
        synchronized (newRenderables) {
            newRenderables.add(renderable);
        }   
    }    
  
    /**
     * When a new renderable is added we need to:
     * - call the static initialiser (in case this is the first time an instance of that
     *   class has been added.  This can initialise shaders and descriptor layouts that
     *   are shared across all instances of each renderable class.
     * - if the renderer has already been initialised itself it will have a CVKDevice, if
     *   that exists we need to initialise the new renderable instance.  If it doesn't 
     *   exist then as long as the renderable is in our renderables list it will get 
     *   initialised when the renderer is initialised.
     * - increment descriptor counts.  We do this even if we don't have a swapchain so
     *   that when the first swapchain is created it will allocate a descriptor pool
     *   big enough for the renderables we currently have.
     * 
     * @return
     */
    private int ProcessNewRenderables() {
        cvkCanvas.VerifyInRenderThread();
        int ret = VK_SUCCESS;
        int oldRenderableCount = renderables.size();
        
        synchronized (newRenderables) {
            for (CVKRenderable r : newRenderables) {
                try {
                    ret = r.Initialise();
                    if (VkFailed(ret)) { return ret; }               
                    renderables.add(r);
                } catch (Exception e) {
                    GetLogger().LogException(e, "Exception initialising %s", r.getClass().getName());
                    return CVK_ERROR_RENDERABLE_INITIALISATION_FAILED;
                }
            }
            newRenderables.clear();
        }
        
        // If we added any new renderables we should recalculate the descriptor requirements
        if (oldRenderableCount != renderables.size()) {
            cvkDescriptorPoolRequirements = new CVKDescriptorPoolRequirements();
            cvkPerImageDescriptorPoolRequirements = new CVKDescriptorPoolRequirements();
            renderables.forEach(r -> {
                r.IncrementDescriptorTypeRequirements(cvkDescriptorPoolRequirements,
                                                      cvkPerImageDescriptorPoolRequirements);
            });
        }
        
        if (cvkDescriptorPool != null) {
            descriptorPoolNeedsRecreation = !cvkDescriptorPool.CanAccomodate(cvkSwapChain.GetImageCount(), cvkDescriptorPoolRequirements, cvkPerImageDescriptorPoolRequirements);                    
        }
        
        return ret;
    }    
    
    
    // ========================> Surface callbacks <======================== \\
    
    public void SurfaceLost() {
        CVKDevice.WaitIdle();
        
        for (CVKRenderable el : renderables) {
            el.SetNewSwapChain(null);
            el.SetNewDescriptorPool(null);
        }
        
        if (cvkSwapChain != null) {                
            cvkSwapChain.Destroy();
            cvkSwapChain = null;
        }
        swapChainNeedsRecreation = true;       
        
        if (cvkDescriptorPool != null) {                
            cvkDescriptorPool.Destroy();
            cvkDescriptorPool = null;
        }
        descriptorPoolNeedsRecreation = true;         
    }    
    
    
    // ========================> Device resources <======================== \\
    
    private int RecreateSwapChain() {        
        CVKAssertNotNull(cvkCanvas);
        CVKAssertNotNull(cvkDescriptorPoolRequirements);
        CVKAssertNotNull(cvkPerImageDescriptorPoolRequirements);
        cvkCanvas.VerifyInRenderThread();  
        
        int ret;

        // Device must be finished with all pending actions before we can
        // recreate the swapchain.
        CVKDevice.WaitIdle();

        // Create a new swapchain.  The number of images shouldn't change, but
        // if they do each renderable will do a full destroy/create of its
        // swapchain dependent resources.
        CVKSwapChain newSwapChain = new CVKSwapChain(cvkCanvas);              
        ret = newSwapChain.Initialise();
        if (VkFailed(ret)) { return ret; }

        // Give each renderable a chance to cleanup swapchain dependent resources
        for (CVKRenderable el : renderables) {
            ret = el.SetNewSwapChain(newSwapChain);
            if (VkFailed(ret)) { return ret; }
        }

        if (cvkSwapChain != null) {                
            cvkSwapChain.Destroy();
        }
        cvkSwapChain = newSwapChain;

        // Update the parent (CVKVisualProcessor) so it can update the shared viewport and frustum
        // These will in turn be consumed by the renderables on their next ProcessRenderTasks
        cvkVisualProcessor.SwapChainRecreated(cvkSwapChain);                                                                                    

        swapChainNeedsRecreation = false;                                       

        return ret;
    }
    
    private int RecreateDescriptorPool() {
        cvkCanvas.VerifyInRenderThread();                        
        CVKAssertNotNull(cvkDescriptorPoolRequirements);
        CVKAssertNotNull(cvkPerImageDescriptorPoolRequirements);
        
        int ret = VK_SUCCESS;

        // Device must be finished with all pending actions before we can
        // recreate the swapchain or descriptor pool
        CVKDevice.WaitIdle();

        CVKDescriptorPool newDescriptorPool = new CVKDescriptorPool(cvkSwapChain.GetImageCount(),
                                                                    cvkDescriptorPoolRequirements,
                                                                    cvkPerImageDescriptorPoolRequirements,
                                                                    GetLogger());
        CVKAssert(newDescriptorPool.GetDescriptorPoolHandle() != VK_NULL_HANDLE);        
        
        for (CVKRenderable el : renderables) {
            ret = el.SetNewDescriptorPool(newDescriptorPool);
            if (VkFailed(ret)) { return ret; }
        }    
        if (cvkDescriptorPool != null) {
            cvkDescriptorPool.Destroy();
        }
        cvkDescriptorPool = newDescriptorPool;

        descriptorPoolNeedsRecreation = false;                                       

        return ret;        
    }    
    
    
    // ========================> Command buffers <======================== \\
    
    /**
     * Records/updates the Primary Command Buffer and all its Secondary Command Buffers
     * 
     * @param stack
     * @param imageIndex - index to get the current frame/image/command buffer
     * @return 
     */
    private int RecordCommandBuffer(MemoryStack stack, int imageIndex){
        cvkCanvas.VerifyInRenderThread();
        CVKAssertNotNull(cvkSwapChain.GetFrameBufferHandle(imageIndex));
    
        int ret = VK_SUCCESS;
        
        VkCommandBuffer primaryCommandBuffer = cvkSwapChain.GetCommandBuffer(imageIndex);
           
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
        beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

        VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
        clearValues.color().float32(stack.floats(clr.getR(), clr.getG(), clr.getB(), 1.0f));
        clearValues.get(1).depthStencil().set(1.0f, 0);
        
        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        renderPassInfo.renderPass(cvkSwapChain.GetRenderPassHandle());

        VkRect2D renderArea = VkRect2D.callocStack(stack);
        renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
        renderArea.extent(cvkSwapChain.GetExtent());
        renderPassInfo.renderArea(renderArea);       
        renderPassInfo.pClearValues(clearValues);
        renderPassInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(imageIndex));

        // The primary command buffer does not contain any rendering commands
	// These are stored (and retrieved) from the secondary command buffers
        ret = vkBeginCommandBuffer(primaryCommandBuffer, beginInfo);
        if (VkFailed(ret)) { return ret; }
        
        vkCmdBeginRenderPass(primaryCommandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);

        // Inheritance info for the secondary command buffers (same for all!)
        VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.callocStack(stack);
        inheritanceInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO);
        inheritanceInfo.pNext(0);
        inheritanceInfo.framebuffer(cvkSwapChain.GetFrameBufferHandle(imageIndex));
        inheritanceInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
        inheritanceInfo.subpass(0); // Get the subpass of make it here?
        inheritanceInfo.occlusionQueryEnable(false);
        inheritanceInfo.queryFlags(0);
        inheritanceInfo.pipelineStatistics(0);
            
        // Loop through renderables and record their buffers
        for (CVKRenderable el : renderables) {
            if (CVK_DEBUGGING && el.DebugSkipRender()) {
                continue;
            }
            if (el.GetVertexCount() > 0) {
                ret = el.RecordDisplayCommandBuffer(inheritanceInfo, imageIndex);
                if (VkFailed(ret)) { return ret; }

                // TODO: may be more efficient to add all the visible command buffers to a master list then 
                // call the following line once with the whole list
                VkCommandBuffer commandBuffer = el.GetDisplayCommandBuffer(imageIndex);
                if (commandBuffer != null) {
                    vkCmdExecuteCommands(primaryCommandBuffer, commandBuffer);
                }
            }
        }
        
        vkCmdEndRenderPass(primaryCommandBuffer);
        checkVKret(vkEndCommandBuffer(primaryCommandBuffer)); 
    
        if (CVK_DEBUGGING) {
            Debug_UpdateRGB();
        }
        
        return ret; 
    }
    
    /**
     *
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param stack
     * @param pImageAcquiredSemaphore
     * @param pCommandBufferExecutedSemaphore
     * @param commandBuffer
     * @param hRenderFence
     * @return
     */
    public int ExecuteCommandBuffer(MemoryStack stack, 
                                    LongBuffer pImageAcquiredSemaphore, 
                                    LongBuffer pCommandBufferExecutedSemaphore, 
                                    VkCommandBuffer commandBuffer,
                                    long hRenderFence) {
        CVKAssert(pImageAcquiredSemaphore != null && pImageAcquiredSemaphore.get(0) != VK_NULL_HANDLE);
        CVKAssert(pCommandBufferExecutedSemaphore != null && pCommandBufferExecutedSemaphore.get(0) != VK_NULL_HANDLE);
        CVKAssert(commandBuffer != null);
        
        VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
        submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
        submitInfo.pCommandBuffers(stack.pointers(commandBuffer));
        submitInfo.pWaitSemaphores(pImageAcquiredSemaphore);
        submitInfo.waitSemaphoreCount(1);
        submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
        submitInfo.pSignalSemaphores(pCommandBufferExecutedSemaphore);   
        
        return vkQueueSubmit(CVKDevice.GetVkQueue(), 
                             submitInfo, 
                             hRenderFence);    
    }    
    
    
    // ========================> Display <======================== \\
    
    /**
     *
     * API calls in this method are asynchronous and need to be synchronised.
     * 
     * @param stack
     * @param pCommandExecutionSemaphore
     * @param imageIndex
     * @return
     */
    private int ReturnImageToSwapchainAndPresent(MemoryStack stack, LongBuffer pCommandExecutionSemaphore, int imageIndex) {
        CVKAssertNotNull(CVKDevice.GetVkQueue());
        
        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
        presentInfo.pWaitSemaphores(pCommandExecutionSemaphore);
        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(cvkSwapChain.GetSwapChainHandle()));
        presentInfo.pImageIndices(stack.ints(imageIndex));        
        return vkQueuePresentKHR(CVKDevice.GetVkQueue(), presentInfo);
    }

    /**
     * No parrellisation of CPU/GPU, eg only one swap chain image and set of command buffers
     * <table cellspacing="2" cellpadding="1" border="1" align="center">
     * <tr><td>CPU</td><td>Prepare image 0</td><td></td><td>Prepare image 0</td><td></td></tr>
     * <tr><td>GPU</td><td></td><td>Present image 0</td><td></td><td>Present image 0</td></tr>
     * </table>
     * <p>
     * 2 swapchain images with synchronisation of CPU preparation and GPU execution/presentation
     * <table cellspacing="2" cellpadding="1" border="1" align="center">
     * <tr><td>CPU</td><td>Prepare image 0</td><td>Prepare image 1</td><td>Prepare image 0</td><td>Prepare image 1</td></tr>
     * <tr><td>GPU</td><td>Present image 1</td><td>Present image 0</td><td>Present image 1</td><td>Present image 0</td></tr>
     * </table>
     * <p>
     * 
     * <h1>SYNCHRONISATION</h1>
     * There are several points that need to be synchronised:
     * <p>
     * 1. Acquiring the next available image from the swap chain.  The image the swap chain
     *    returns to us depends on the state of the images in flight and the policy used to
     *    create the swap chain.  See the initialisation of CVKDevice.selectedPresentationMode
     *    for more details.
     *    vkAcquireNextImageKHR will return immediately with the index of the image that will be
     *    acquired for rendering and presenting, but the image itself may not be ready yet to be
     *    rendered to or presented.  For that we need to provide either a fence or semaphore to
     *    vkAcquireNextImageKHR that will be signaled when the image is ready.  We can pass that
     *    semaphore into vkQueueSubmit when we submit our command buffers so the execution of
     *    commands waits until the image is ready.
     * <p>
     * 2. Presenting an image.  vkQueuePresentKHR adds an acquired image to the presentation queue
     *    which once drained unacquires the image and returns it to the swapchain.  Before we 
     *    enqueue an image into the presentation queue we need to ensure our rendering (execution
     *    of command buffers) has completed.
     * 
     * Frame 0:
     * 1. acquire image index 0  (get imageAcquisitionSemaphore 0)
     * 2. wait on that image 0 being ready
     * 3. execute command buffer 0 (return imageAcquisitionSemaphore 0 and get commandBufferExecutedSemaphore 0)
     * 4. wait for command buffer 0 to execute
     * 5. present image 0
     * 
     * !Problem: commandBufferExecutedSemaphore is never returned.  Maybe it never needs to be returned as we
     * have to wait for imageAcquisitionSemaphore 0 which implies commandBufferExecutedSemaphore 0 will have 
     * already been signaled.  If that is the case then we only need logic for the image acquisition semaphore.
     * We could bind them together, like the CVKFrame concept.
     * 
     * Frame 1:
     * 1. acquire image index 1
     * 2. wait on that image 1 being ready
     * 3. execute command buffer 1
     * 4. wait for command buffer 1 to execute
     * 5. present image 1
     * 
     * Frame 2
     * 
     * 0. wait on imageAcquisitionSemaphore 0, is this necessary?
     * 1. acquire image index 0 
     * 2. wait on that image 0 being ready
     * 3. execute command buffer 0
     * 4. wait for command buffer 0 to execute
     * 5. present image 0
     * 
     * Commands may be enqueued in order but without synchronisation they can be processed and
     * complete in any order.  We often require a particular state before an operation occurs,
     * for example we require an image is transitioned to the destination transfer optimal 
     * state before we copy data into it.  In order to enforce required states we use pipeline
     * barriers.  VkImageMemoryBarrier is the type of barrier we use in the previous example,
     * not only does this barrier block latter commands until our destination stage is reached,
     * it is also responsible for the transition from source to destination state.
     * 
     * Pipeline barriers (image transitions).  
     * 
     * 
     */
    public void Display() {
        int ret;
        
        if (isExiting) {
            if (CVKDevice.GetVkDevice() != null) {
                CVKDevice.WaitIdle();
                DestroyImplementation();
                exitComplete.countDown();
            }
            return;
        }
        
        // Our render thread should be the AWT thread that owns the canvas, whose
        // surface is our render target.  Being called by any other thread will 
        // lead to resource contention and deadlock (seen during development when
        // user events were handled immediately rather than enqueueing them for
        // the render thread to handle).
        cvkCanvas.VerifyInRenderThread();
        
        // Adding renderables will change descriptor requirements, we need to update
        // these before the swapchain is initialised for the first time.
        ret = ProcessNewRenderables();
        checkVKret(ret);        

        if (cvkSwapChain == null || swapChainNeedsRecreation) {
            ret = RecreateSwapChain();
            if (VkFailed(ret)) {
                GetLogger().warning("recreate swapchain failed, skipping this display");
                return;
            }            
        }
        if (cvkDescriptorPool == null || descriptorPoolNeedsRecreation) {
            ret = RecreateDescriptorPool();
            checkVKret(ret); 
        }           
        
        if (cvkSwapChain != null) {
            // Process updates enqueued by other threads.  If any display resources need to be
            // (re)created then the renderable involved needs to set a flag so the next time
            // NeedsDisplayUpdate is called it returns true and is given a chance to update 
            // while no GPU actions are pending.
            ret = cvkVisualProcessor.ProcessRenderTasks(cvkSwapChain);
            checkVKret(ret);               
            
            // If any renderable holds a resource shared across frames then to
            // recreate it we need to a complete halt, that is we need all in
            // flight images to have been presented and all fences available.
            // Note the one liner was created by Netbeans, I am not convinced it's
            // very clear.
            boolean updateDisplays = false;
            for (CVKRenderable el : renderables) {
                updateDisplays = el.NeedsDisplayUpdate();
                if (updateDisplays) break;
            }
                    
            // Blocking updates
            if (updateDisplays) {
                CVKDevice.WaitIdle();
                ret = CVKIconTextureAtlas.GetInstance().DisplayUpdate();
                checkVKret(ret);
                ret = CVKGlyphTextureAtlas.GetInstance().DisplayUpdate();
                checkVKret(ret);
                for (CVKRenderable el : renderables) {
                    if (el.NeedsDisplayUpdate()) {
                        ret = el.DisplayUpdate();
                        checkVKret(ret); 
                    }
                }                
            } else {
                // TODO: remove once the shouldRender code is figured out.
                return;
            }
        }
   
        // If the surface is not ready RecreateSwapChain won't have reset this flag        
        if (cvkSwapChain != null && !swapChainNeedsRecreation && cvkDescriptorPool != null && !descriptorPoolNeedsRecreation) {                    
            try (MemoryStack stack = stackPush()) {                
                // The swapchain decides which image we should render to based on the
                // mode we created with.
                IntBuffer pImageIndex = stack.mallocInt(1);
                LongBuffer pImageAcquisitionSemaphore = stack.mallocLong(1);
                ret = cvkSwapChain.AcquireNextImage(stack, pImageIndex, pImageAcquisitionSemaphore);
                if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                 || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                    swapChainNeedsRecreation = true;
                } else {
                    checkVKret(ret);                    
                    int imageIndex = pImageIndex.get(0);
                    
                    // The two semaphores tell us when an image is ready and when we've finished writing 
                    // to our command buffers, they don't tell us when the GPU is finished with the command
                    // buffers.  For that we need a fence, this is a synchronisation structure that is 
                    // device writable and host readable.
                    ret = cvkSwapChain.WaitOnFence(imageIndex);
                    checkVKret(ret);                                   
                    
                    // Record each renderables commands into secondary buffers and add them to the
                    // primary command buffer.
                    ret = RecordCommandBuffer(stack, imageIndex);
                    checkVKret(ret); 
                                                            
                    // This will wait for the image at imageIndex to be acquired then submit
                    // our primary command buffer to the execution queue.  Once executed 
                    // hCommandBufferExecutedSemaphore will be signaled and we can present.
                    LongBuffer pCommandExecutionSemaphore = stack.mallocLong(1);
                    ret = cvkSwapChain.GetCommandBufferExecutedSemaphore(imageIndex, pCommandExecutionSemaphore);
                    checkVKret(ret);
                    ret = ExecuteCommandBuffer(stack, 
                                               pImageAcquisitionSemaphore, 
                                               pCommandExecutionSemaphore,
                                               cvkSwapChain.GetCommandBuffer(imageIndex),
                                               cvkSwapChain.GetFence(imageIndex));
                    checkVKret(ret);             
                    
                    // Render fences synchronise CPU-GPU access to shared memory.  The
                    // fence we want to use will be in the signalled state as either it
                    // was just created (we create them in the signalled state) or the 
                    // GPU will have signalled it's finished with.  We now reset it to
                    // the unsignalled state prior to ReturnImageToSwapchainAndPresent 
                    // so that we can wait on the GPU to signal it again.
                    //frame.ResetRenderFence();
                    ret = ReturnImageToSwapchainAndPresent(stack,
                                                           pCommandExecutionSemaphore,
                                                           imageIndex);
                    if (ret == CVKMissingEnums.VkResult.VK_SUBOPTIMAL_KHR.Value()
                     || ret == CVKMissingEnums.VkResult.VK_ERROR_OUT_OF_DATE_KHR.Value()) {
                        swapChainNeedsRecreation = true;
                    } else {
                        checkVKret(ret);
                    }
                    
                    // Offscreen Render Pass
                    List<CVKRenderable> hitTestRenderables = cvkVisualProcessor.GetHitTesterList();
                    ret = cvkVisualProcessor.GetHitTester().OffscreenRender(hitTestRenderables);
                    checkVKret(ret);                  
        
                    if (requestScreenshot) {
                        CVKDevice.WaitIdle();
                        cvkSwapChain.GetImage(imageIndex).SaveToFile(requestScreenshotFile);
                        requestScreenshot = false;
                        requestScreenshotFile = null;
                    }
                    
                    // Move the frame index to the next cab off the rank
                    currentFrame = (++currentFrame) % cvkSwapChain.GetImageCount();                  
                }
            }
        }        
        
        // We'll need another frame to recreate the swapchain.  swapChainNeedsRecreation
        // can be set if submitting an image to the render queue returned suboptimal or out of date.
        if (swapChainNeedsRecreation) {
            cvkVisualProcessor.requestRedraw();
        }             
        
        // The visual processor will not trigger any more updates until this is signalled
        cvkVisualProcessor.signalProcessorIdle();
    }    
    
   /**
     * @param file to save the screenshot to
     * @return 
     */
    public CVKRenderUpdateTask TaskRequestScreenshot(File file) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
            
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            requestScreenshot = true;
            requestScreenshotFile = file;
        };
    }
    
    public CVKRenderUpdateTask BackgroundColourChanged(final VisualChange change, final VisualAccess access) {
        final ConstellationColor backgroundColour = access.getBackgroundColor();
        
        return () -> {
            clr.setR(backgroundColour.getRed());
            clr.setG(backgroundColour.getGreen());
            clr.setB(backgroundColour.getBlue());
        };
    }    

    int[] getViewport() {
        final int[] viewport = new int[4];
        viewport[0] = 0;
        viewport[1] = 0;
        viewport[2] = cvkSwapChain.GetWidth();
        viewport[3] = cvkSwapChain.GetHeight();
        return viewport;
    }    
    
    private void Debug_UpdateRGB(){   
        final float MAX = 0.7f;  // don't go to white, it's hard to see points
        // When the current component reaches it's limit do something
        if (clr.a[curClrEl] >= MAX || clr.a[curClrEl] < 0.0f) {
            // Clamp it to 0-1
            clr.a[curClrEl] = Math.max(0.0f, Math.min(MAX, clr.a[curClrEl]));
            // If we have hit the ceiling or floor on all components, change direction
            if (curClrEl == 2) {
                clrChange = -clrChange;
            }
            // Start changing the next component
            curClrEl = (curClrEl + 1) % 3;
        }
        // Walk the current component a little
        clr.a[curClrEl] += clrChange;            
    }
     
    
    // ========================> Component listener <======================== \\
    
    @Override
    public void componentResized(ComponentEvent e) {
        // Hydra - This callback is quite spammy, resulting in the swapchain being
        // recreated many times in a row. However, there is a bug in some of
        // the Vulkan drivers where the VK_SUBOPTIMAL_KHR and 
        // VK_ERROR_OUT_OF_DATE_KHR messages are not being generated from
        // vkAcquireNextImageKHR() or vkQueuePresentKHR. Updating the GFX drivers
        // fixed the issue on my system. Unfortunately we can't expect users
        // to be running with the correct drivers so instead we have to do it
        // this way to flag a recreation.
        // TODO: Test how this performs when we have a big graph
        
        // Reference: https://bugs.freedesktop.org/show_bug.cgi?id=111097
        swapChainNeedsRecreation = true;
    }
    @Override
    public void componentHidden(ComponentEvent e) {
        GetLogger().info("Canvas sent ");
    }
    @Override
    public void componentMoved(ComponentEvent e) {
        GetLogger().info("Canvas moved");     
    }
    @Override
    public void componentShown(ComponentEvent e) {
        GetLogger().info("Canvas shown");
    }           
}
