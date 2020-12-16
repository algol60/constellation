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
package au.gov.asd.tac.constellation.visual.vulkan.renderables;

import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_CLEAN;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_UPDATE;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNotNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssertNull;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkFreeDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkWriteDescriptorSet;


/*******************************************************************************
 * CVKPerspectiveLinksRenderable
 * 
 * This class renders links between nodes as triangles.  It is paired with 
 * CVKLinksRenderable which renders links between nodes with lines.  This class 
 * uses triangles in order to achieve perspective and to add direction arrows.
 * 
 * These bonded pair of classes fulfill the same role as au.gov.asd.tac.constellation.
 * visual.opengl.renderer.batcher.LineBatcher in the JOGL version.
 * 
 * LineBatcher draws both a line and triangle-line between two nodes.  When the 
 * camera is close only the triangle-line is visible.  As the camera pans out
 * the triangle-line becomes less visible eventually becoming sub-pixel.  The
 * line which was previously not visible becomes visible.  It's surprisingly 
 * effective.
 * 
 * The reason this was split into two renderables in the Vulkan display project
 * is that CVKRenderables encapsulate a single pipeline which have a single shader
 * for each assembly step (vert, geo, frag).  Subsequently they only output a single
 * primitive type (triangle_strip, line_list, point_list etc).  So rather than
 * introduce the complexity of multiple display pipelines into the rest of the 
 * renderables we just share data and have a renderable for each pipeline.
 *******************************************************************************/

public class CVKPerspectiveLinksRenderable extends CVKRenderable {
    // CVKLinks is the master in this master-slave relationship
    private final CVKLinksRenderable cvkLinks;
    
    // Resources recreated with the swap chain (dependent on the image count)    
    private LongBuffer pDescriptorSets = null; 
    private List<Long> hitTestPipelines = null;
    private List<CVKCommandBuffer> displayCommandBuffers = null;
    private List<CVKCommandBuffer> hittestCommandBuffers = null;      

    // Resources we don't own but use and must track so we know when to update
    // our descriptors
    private long hPositionBuffer = VK_NULL_HANDLE;
    private long hPositionBufferView = VK_NULL_HANDLE;    
    
    // Push constants for shaders contains the MV matrix and drawHitTest int
    private static final int MODEL_VIEW_PUSH_CONSTANT_STAGES = VK_SHADER_STAGE_VERTEX_BIT;
    private static final int HIT_TEST_PUSH_CONSTANT_STAGES = VK_SHADER_STAGE_GEOMETRY_BIT | VK_SHADER_STAGE_FRAGMENT_BIT;

    
    
    // ========================> Classes <======================== \\
   
    @Override
    protected VkVertexInputBindingDescription.Buffer GetVertexBindingDescription() {
        return CVKLinksRenderable.Vertex.GetBindingDescription();
    }
    
    @Override
    protected VkVertexInputAttributeDescription.Buffer GetVertexAttributeDescriptions() {
        return CVKLinksRenderable.Vertex.GetAttributeDescriptions();
    }        
                       
    
    // ========================> Shaders <======================== \\
    
    @Override
    protected String GetVertexShaderName() { return "Line.vs"; }
    
    @Override
    protected String GetGeometryShaderName() { return "Line.gs"; }
    
    @Override
    protected String GetFragmentShaderName() { return "Line.fs"; }   
        
    
    // ========================> Lifetime <======================== \\
    
    public CVKPerspectiveLinksRenderable(CVKLinksRenderable links) {
        super(links.cvkVisualProcessor);
        
        cvkLinks = links;
        
        // Assembly
        assemblyTopology = VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
    }              
    
    @Override
    public int Initialise() {
        int ret = super.Initialise();
        if (VkFailed(ret)) { return ret; }
        
        // Check for double initialisation
        CVKAssert(hDescriptorLayout == VK_NULL_HANDLE);    
        
        ret = CreateDescriptorLayout();
        if (VkFailed(ret)) { return ret; }   
        
        ret = CreatePipelineLayout();
        if (VkFailed(ret)) { return ret; }          
        
        return ret;
    }        
   
    
    @Override
    public void Destroy() {
        DestroyDescriptorSets();
        DestroyDescriptorLayout();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyCommandBuffers();
        
        CVKAssertNull(pDescriptorSets);
        CVKAssertNull(hDescriptorLayout);  
        CVKAssertNull(displayCommandBuffers);        
        CVKAssertNull(displayPipelines);
        CVKAssertNull(hPipelineLayout);    
    }
    
       
    // ========================> Swap chain <======================== \\
       
    @Override
    protected int DestroySwapChainResources() { 
        this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (displayPipelines != null && swapChainImageCountChanged) {  
            DestroyDescriptorSets();
            DestroyCommandBuffers();     
            DestroyPipelines();                              
        }
        
        return VK_SUCCESS; 
    }
    
    @Override
    public int SetNewSwapChain(CVKSwapChain newSwapChain) {
        int ret = super.SetNewSwapChain(newSwapChain);
        if (VkFailed(ret)) { return ret; }
        
        if (swapChainImageCountChanged) {
            // The number of images has changed, we need to rebuild all image
            // buffered resources
            SetCommandBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetDescriptorSetsState(CVK_RESOURCE_NEEDS_REBUILD);
            SetPipelinesState(CVK_RESOURCE_NEEDS_REBUILD);
        }
        
        return ret;
    } 
    
    
    // ========================> Vertex buffers <======================== \\
          
    @Override
    public int GetVertexCount() { return cvkLinks.GetVertexCount(); }                   
       
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssertNotNull(cvkSwapChain);
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        displayCommandBuffers = new ArrayList<>(imageCount);
        hittestCommandBuffers = new ArrayList<>(imageCount);
        
        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKPerspectiveLinksRenderable Display Command Buffer %d", i));
            displayCommandBuffers.add(buffer);
            
            CVKCommandBuffer offscreenBuffer = CVKCommandBuffer.Create(VK_COMMAND_BUFFER_LEVEL_SECONDARY, GetLogger(), String.format("CVKPerspectiveLinksRenderable Offscreen Command Buffer %d", i));
            hittestCommandBuffers.add(offscreenBuffer);
        }
        
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }   
    
    @Override
    public VkCommandBuffer GetDisplayCommandBuffer(int imageIndex) {
        return displayCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }       
    
    @Override
    public VkCommandBuffer GetHitTestCommandBuffer(int imageIndex) {
        return hittestCommandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }
    
    @Override
    public int RecordDisplayCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(CVKDevice.GetCommandPoolHandle());
        CVKAssertNotNull(cvkSwapChain);
                
        int ret;     
         
        CVKCommandBuffer commandBuffer = displayCommandBuffers.get(imageIndex);
        CVKAssert(commandBuffer != null);
        CVKAssert(displayPipelines.get(imageIndex) != null);

        commandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT, inheritanceInfo);

        commandBuffer.SetViewPort(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight());
        commandBuffer.SetScissor(cvkVisualProcessor.GetCanvas().GetCurrentSurfaceExtent());

        commandBuffer.BindGraphicsPipeline(displayPipelines.get(imageIndex));
        commandBuffer.BindVertexInput(cvkLinks.GetVertexBufferHandle(imageIndex));

        // Push MV matrix to the vertex shader
        commandBuffer.PushConstants(hPipelineLayout, 
                                    MODEL_VIEW_PUSH_CONSTANT_STAGES, 
                                    0, 
                                    cvkLinks.GetModelViewPushConstants());

        // Push drawHitTest flag to the geometry and fragment shader
        commandBuffer.PushConstants(hPipelineLayout, 
                                    HIT_TEST_PUSH_CONSTANT_STAGES, 
                                    cvkLinks.GetModelViewPushConstantsSize(), 
                                    cvkLinks.GetHitTestPushConstants());

        commandBuffer.BindGraphicsDescriptorSets(hPipelineLayout, pDescriptorSets.get(imageIndex));

        commandBuffer.Draw(GetVertexCount());

        ret = commandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }
        
        return ret;
    }
    
    @Override
    public int RecordHitTestCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        cvkVisualProcessor.VerifyInRenderThread();
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssert(CVKDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);
        CVKAssertNotNull(cvkSwapChain);

        int ret;     
        
        // Set the hit test flag in the shaders to true
        cvkLinks.UpdatePushConstantsHitTest(true);            

        CVKCommandBuffer commandBuffer = hittestCommandBuffers.get(imageIndex);          
        CVKAssertNotNull(commandBuffer);
        CVKAssertNotNull(hitTestPipelines.get(imageIndex));

        ret = commandBuffer.BeginRecordSecondary(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT,
                                                       inheritanceInfo);
        if (VkFailed(ret)) { return ret; }

        commandBuffer.SetViewPort(cvkSwapChain.GetWidth(), cvkSwapChain.GetHeight());
        commandBuffer.SetScissor(cvkVisualProcessor.GetCanvas().GetCurrentSurfaceExtent());

        commandBuffer.BindGraphicsPipeline(hitTestPipelines.get(imageIndex));
        commandBuffer.BindVertexInput(cvkLinks.GetVertexBufferHandle(imageIndex));

        // Push MV matrix to the vertex shader
        commandBuffer.PushConstants(hPipelineLayout, 
                                    MODEL_VIEW_PUSH_CONSTANT_STAGES, 
                                    0, 
                                    cvkLinks.GetModelViewPushConstants());

        // Push drawHitTest flag to the geometry and fragment shader
        commandBuffer.PushConstants(hPipelineLayout, 
                                    HIT_TEST_PUSH_CONSTANT_STAGES, 
                                    cvkLinks.GetModelViewPushConstantsSize(), 
                                    cvkLinks.GetHitTestPushConstants());
        
        commandBuffer.BindGraphicsDescriptorSets(hPipelineLayout, pDescriptorSets.get(imageIndex));

        commandBuffer.Draw(GetVertexCount());

        ret = commandBuffer.FinishRecord();
        if (VkFailed(ret)) { return ret; }

        // Reset hit test flag to false
        cvkLinks.UpdatePushConstantsHitTest(false);
        
        return ret;
    }
    
    private void DestroyCommandBuffers() {         
        if (null != displayCommandBuffers) {
            displayCommandBuffers.forEach(el -> {el.Destroy();});
            displayCommandBuffers.clear();
            displayCommandBuffers = null;
        }
        
        if (null != hittestCommandBuffers) {
            hittestCommandBuffers.forEach(el -> {el.Destroy();});
            hittestCommandBuffers.clear();
            hittestCommandBuffers = null;
        }  
    }        
    
        
    // ========================> Descriptors <======================== \\
    
    private int CreateDescriptorLayout() {
        int ret;
        
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(3, stack);

            // 0: Vertex uniform buffer
            VkDescriptorSetLayoutBinding vertexUBDSLB = bindings.get(0);
            vertexUBDSLB.binding(0);
            vertexUBDSLB.descriptorCount(1);
            vertexUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            vertexUBDSLB.pImmutableSamplers(null);
            vertexUBDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            
            // 1: Vertex samplerBuffer (position buffer)
            VkDescriptorSetLayoutBinding vertexSamplerDSLB = bindings.get(1);
            vertexSamplerDSLB.binding(1);
            vertexSamplerDSLB.descriptorCount(1);
            vertexSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
            vertexSamplerDSLB.pImmutableSamplers(null);
            vertexSamplerDSLB.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);            
            
            // 2: Geometry uniform buffer
            VkDescriptorSetLayoutBinding geometryUBDSLB = bindings.get(2);
            geometryUBDSLB.binding(2);
            geometryUBDSLB.descriptorCount(1);
            geometryUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geometryUBDSLB.pImmutableSamplers(null);
            geometryUBDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);                
                          

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            ret = vkCreateDescriptorSetLayout(CVKDevice.GetVkDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
                GetLogger().info("CVKPerspectiveLinksRenderable created hDescriptorLayout: 0x%016X", hDescriptorLayout);
            }
        }        
        return ret;
    }      
    
    private void DestroyDescriptorLayout() {
        GetLogger().info("CVKPerspectiveLinksRenderable destroying hDescriptorLayout: 0x%016X", hDescriptorLayout);
        vkDestroyDescriptorSetLayout(CVKDevice.GetVkDevice(), hDescriptorLayout, null);
        hDescriptorLayout = VK_NULL_HANDLE;
    }
    
    private int CreateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(cvkSwapChain);
        
        int ret;    

        // The same layout is used for each descriptor set. Each image has a 
        // an identical copy of the descriptor set so allow the GPU and CPU to
        // desynchronise (when there are 2 or more images in the swapchain).
        final int imageCount = cvkSwapChain.GetImageCount();
        LongBuffer layouts = stack.mallocLong(imageCount);
        for (int i = 0; i < imageCount; ++i) {
            layouts.put(i, hDescriptorLayout);
        }

        VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
        allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
        allocInfo.descriptorPool(cvkDescriptorPool.GetDescriptorPoolHandle());
        allocInfo.pSetLayouts(layouts);

        // Allocate the descriptor sets from the descriptor pool, they'll be unitialised
        pDescriptorSets = MemoryUtil.memAllocLong(imageCount);
        ret = vkAllocateDescriptorSets(CVKDevice.GetVkDevice(), allocInfo, pDescriptorSets);
        if (VkFailed(ret)) { return ret; }
        
        for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
            GetLogger().info("CVKPerspectiveLinksRenderable allocated hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
        }
        
        return UpdateDescriptorSets(stack);
    }
    
    // TODO: do we gain anything by having buffered UBOs?
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssertNotNull(cvkSwapChain);
        CVKAssertNotNull(cvkDescriptorPool);
        CVKAssertNotNull(pDescriptorSets);
        CVKAssert(pDescriptorSets.capacity() > 0);
//        CVKAssertNotNull(vertexUniformBuffers);
//        CVKAssert(vertexUniformBuffers.size() > 0);
//        CVKAssertNotNull(geometryUniformBuffers);
//        CVKAssert(geometryUniformBuffers.size() > 0);   
        
        int ret = VK_SUCCESS;
     
        final int imageCount = cvkSwapChain.GetImageCount();
        
        final long positionBufferSize = cvkVisualProcessor.GetPositionBufferSize();
        hPositionBuffer = cvkVisualProcessor.GetPositionBufferHandle();
        hPositionBufferView = cvkVisualProcessor.GetPositionBufferViewHandle();  
        CVKAssertNotNull(hPositionBuffer);
        CVKAssertNotNull(hPositionBufferView);        
        
        // - Descriptor info structs -
        // We create these to describe the different resources we want to address
        // in shaders.  We have one info struct per resource.  We then create a 
        // write descriptor set structure for each resource for each image.  For
        // buffered resources like the the uniform buffers we wait to set the 
        // buffer resource until the image loop below.
        
        // Struct for the uniform buffer used by VertexIcon.vs
        VkDescriptorBufferInfo.Buffer vertexUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // vertexUniformBufferInfo.buffer is set per imageIndex
        vertexUniformBufferInfo.offset(0);
        vertexUniformBufferInfo.range(CVKLinksRenderable.VertexUniformBufferObject.SizeOf());        
        
        // Struct for texel buffer (positions) used by VertexIcon.vs
        VkDescriptorBufferInfo.Buffer positionsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        positionsTexelBufferInfo.buffer(hPositionBuffer);
        positionsTexelBufferInfo.offset(0);
        positionsTexelBufferInfo.range(positionBufferSize);               

        // Struct for the uniform buffer used by VertexIcon.gs
        VkDescriptorBufferInfo.Buffer geometryUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // geometryBufferInfo.buffer is set per imageIndex
        geometryUniformBufferInfo.offset(0);
        geometryUniformBufferInfo.range(CVKLinksRenderable.GeometryUniformBufferObject.SizeOf());                      

        // We need 3 write descriptors, 2 for uniform buffers and 1 for texel buffers                  
        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(3, stack);

        // Vertex uniform buffer
        VkWriteDescriptorSet vertexUBDescriptorWrite = descriptorWrites.get(0);
        vertexUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        vertexUBDescriptorWrite.dstBinding(0);
        vertexUBDescriptorWrite.dstArrayElement(0);
        vertexUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        vertexUBDescriptorWrite.descriptorCount(1);
        vertexUBDescriptorWrite.pBufferInfo(vertexUniformBufferInfo);      
        
        // Vertex texel buffer (positions)
        VkWriteDescriptorSet positionsTBDescriptorWrite = descriptorWrites.get(1);
        positionsTBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        positionsTBDescriptorWrite.dstBinding(1);
        positionsTBDescriptorWrite.dstArrayElement(0);
        positionsTBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
        positionsTBDescriptorWrite.descriptorCount(1);
        positionsTBDescriptorWrite.pBufferInfo(positionsTexelBufferInfo);               
        positionsTBDescriptorWrite.pTexelBufferView(stack.longs(hPositionBufferView));
        
        // Geometry uniform buffer
        VkWriteDescriptorSet geometryUBDescriptorWrite = descriptorWrites.get(2);
        geometryUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        geometryUBDescriptorWrite.dstBinding(2);
        geometryUBDescriptorWrite.dstArrayElement(0);
        geometryUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        geometryUBDescriptorWrite.descriptorCount(1);
        geometryUBDescriptorWrite.pBufferInfo(geometryUniformBufferInfo);     
        
        for (int i = 0; i < imageCount; ++i) {                        
            // Update the buffered resource buffers
            vertexUniformBufferInfo.buffer(cvkLinks.GetVertexUniformBufferHandle(i));
            geometryUniformBufferInfo.buffer(cvkLinks.GetGeometryUniformBufferHandle(i));
                    
            // Set the descriptor set we're updating in each write struct
            long descriptorSet = pDescriptorSets.get(i);
            descriptorWrites.forEach(el -> {el.dstSet(descriptorSet);});

            // Update the descriptors with a write and no copy
            GetLogger().info("CVKPerspectiveLinksRenderable updating descriptorSet: 0x%016X", descriptorSet);
            vkUpdateDescriptorSets(CVKDevice.GetVkDevice(), descriptorWrites, null);
        }           
        
        SetDescriptorSetsState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }
        
    private int DestroyDescriptorSets() {
        int ret = VK_SUCCESS;
        
        if (pDescriptorSets != null) {
            CVKAssertNotNull(cvkDescriptorPool);
            CVKAssertNotNull(cvkDescriptorPool.GetDescriptorPoolHandle());            
            GetLogger().fine("CVKPerspectiveLinksRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity());
            
            for (int i = 0; i < pDescriptorSets.capacity(); ++i) {
                GetLogger().info("CVKPerspectiveLinksRenderable freeing hDescriptorSet %d: 0x%016X", i, pDescriptorSets.get(i));
            }            
            
            // After calling vkFreeDescriptorSets, all descriptor sets in pDescriptorSets are invalid.
            ret = vkFreeDescriptorSets(CVKDevice.GetVkDevice(), cvkDescriptorPool.GetDescriptorPoolHandle(), pDescriptorSets);
            pDescriptorSets = null;
            checkVKret(ret);
        }
        
        return ret;
    }    
    
    @Override
    public int DestroyDescriptorPoolResources() {
        int ret = VK_SUCCESS;
        
        if (cvkDescriptorPool != null) {
            return DestroyDescriptorSets();
        }
        
        return ret;        
    }
    
    @Override
    public void IncrementDescriptorTypeRequirements(CVKDescriptorPoolRequirements reqs, CVKDescriptorPoolRequirements perImageReqs) {
        // Line.vs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // LineLine.gs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        
        // One set per image
        ++perImageReqs.poolDesciptorSetCount;
    }      
    
    @Override
    public int SetNewDescriptorPool(CVKDescriptorPool newDescriptorPool) {
        int ret = super.SetNewDescriptorPool(newDescriptorPool);
        if (VkFailed(ret)) { return ret; }
        SetDescriptorSetsState(CVK_RESOURCE_NEEDS_REBUILD);
        return ret;
    }
    
    
    // ========================> Pipelines <======================== \\
    
    private int CreatePipelineLayout() {
        CVKAssertNotNull(CVKDevice.GetVkDevice());
        CVKAssertNotNull(hDescriptorLayout);
               
        int ret;       
        try (MemoryStack stack = stackPush()) {              
            VkPushConstantRange.Buffer pushConstantRange;
            pushConstantRange = VkPushConstantRange.calloc(2);
            pushConstantRange.get(0).stageFlags(MODEL_VIEW_PUSH_CONSTANT_STAGES);
            pushConstantRange.get(0).size(cvkLinks.GetModelViewPushConstantsSize());
            pushConstantRange.get(0).offset(0);

            pushConstantRange.get(1).stageFlags(HIT_TEST_PUSH_CONSTANT_STAGES);
            pushConstantRange.get(1).size(cvkLinks.GetHitTestPushConstantsSize());
            pushConstantRange.get(1).offset(cvkLinks.GetModelViewPushConstantsSize());           

            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));
            pipelineLayoutInfo.pPushConstantRanges(pushConstantRange);
            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            ret = vkCreatePipelineLayout(CVKDevice.GetVkDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (VkFailed(ret)) { return ret; }
            hPipelineLayout = pPipelineLayout.get(0);
            CVKAssert(hPipelineLayout != VK_NULL_HANDLE);                
        }        
        return ret;        
    }    
    
    private void DestroyPipelineLayout() {
        if (hPipelineLayout != VK_NULL_HANDLE) {
            vkDestroyPipelineLayout(CVKDevice.GetVkDevice(), hPipelineLayout, null);
            hPipelineLayout = VK_NULL_HANDLE;
        }
    }      


    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() { 
        // Race condition: icons owns the position buffer and the update task (on
        // the visual processor thread) may not yet be complete.  If this happens
        // before the first update is finished icons will have a vertexCount of 
        // 0 so it won't be able to create the position buffer yet.  If we can't
        // get a handle to the current position buffer then we just skip updating
        // until we can.
        if (cvkVisualProcessor.GetPositionBufferHandle() == VK_NULL_HANDLE) {
            return false;
        } 
        
        if (hPositionBuffer != cvkVisualProcessor.GetPositionBufferHandle() ||
            hPositionBufferView != cvkVisualProcessor.GetPositionBufferViewHandle()) {
            if (descriptorSetsState != CVK_RESOURCE_NEEDS_REBUILD) {
                descriptorSetsState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        }                      
        
//!! Do we need to track the UBO and vertex states of links some how?
// Could change renderer to cache the results of NeedsDisplayUpdate and use that
// to decide which renderables to call DisplayUpdate on.  This would allow
// CVKPerspectiveLinksRenderable to defer to CVKLinksRenderable.
        return cvkLinks.GetVertexCount() > 0 &&
               (commandBuffersState != CVK_RESOURCE_CLEAN ||
                descriptorSetsState != CVK_RESOURCE_CLEAN ||
                pipelinesState != CVK_RESOURCE_CLEAN);                
    }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        cvkVisualProcessor.VerifyInRenderThread();
                         
        try (MemoryStack stack = stackPush()) {                 
            // Descriptors (binding values to shaders parameters)
            if (descriptorSetsState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; }  
            } else if (descriptorSetsState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateDescriptorSets(stack);
                if (VkFailed(ret)) { return ret; }               
            }  
        
            // Command buffers (rendering commands enqueued on the GPU)
            if (commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateCommandBuffers();
                if (VkFailed(ret)) { return ret; }  
            }           
        
            // Pipelines (all the render state and resources in one object)
            if (pipelinesState == CVK_RESOURCE_NEEDS_REBUILD) {
                displayPipelines = new ArrayList<>(cvkSwapChain.GetImageCount());
                ret = CreatePipelines(cvkSwapChain.GetRenderPassHandle(), displayPipelines);
                if (VkFailed(ret)) { return ret; }
 
                hitTestPipelines = new ArrayList<>(cvkSwapChain.GetImageCount());
                ret = CreatePipelines(cvkSwapChain.GetOffscreenRenderPassHandle(), hitTestPipelines);
                if (VkFailed(ret)) { return ret; }                  
            }                                            
        }                                     
        
        return ret;
    }        
    
    
    // ========================> Tasks <======================== \\    
    // NOTE:  we effectively have two levels of staging.  The second level is pretty
    // straightforward, we need the resources we render with to be in the most
    // optimal memory possible: resident GPU memory.  This memory cannot be written
    // by the CPU so we can't update it directly, instead we update staging buffers
    // that are both GPU and CPU writable then copy from that into the final GPU
    // buffers.
    // The first level of staging is required as our staging buffers are VkDevice
    // resources and the device may not be initialised when these tasks are called.
    // This is the case when a graph is loaded into a new tab, we need to be able
    // to process the tasks that load the graph vertices etc before the device is
    // ready to create staging buffers.  For this reason we update local arrays
    // in the Build<blah>Array functions, then copy these to our staging buffers
    // during the renderer's display loop (when the rendering lambda of each task
    // is executed).  This also means we don't need to synchronise these arrays
    // created by the visual processor's thread with the staging buffers that have
    // a lifespan entirely within the rendering thread (AWT event thread).  This
    // is possible because the arrays are locals in the task functions and aren't
    // modified by the visual processor thread after they've been added to the 
    // queue of tasks for the renderer to process.

    

    
    
    // ========================> Helpers <======================== \\
}
