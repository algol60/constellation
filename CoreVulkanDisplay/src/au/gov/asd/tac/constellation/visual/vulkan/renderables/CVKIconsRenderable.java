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

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4f;
import au.gov.asd.tac.constellation.utilities.graphics.Vector4i;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDescriptorPool.CVKDescriptorPoolRequirements;
import au.gov.asd.tac.constellation.visual.vulkan.CVKDevice;
import au.gov.asd.tac.constellation.visual.vulkan.CVKSwapChain;
import au.gov.asd.tac.constellation.visual.vulkan.utils.CVKShaderUtils;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkSucceeded;
import au.gov.asd.tac.constellation.visual.vulkan.CVKVisualProcessor;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_CLEAN;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableResourceState.CVK_RESOURCE_NEEDS_UPDATE;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKBuffer.DEBUG_CVKBufferElementDescriptor;
import au.gov.asd.tac.constellation.visual.vulkan.resourcetypes.CVKCommandBuffer;
import au.gov.asd.tac.constellation.visual.vulkan.shaders.CVKShaderPlaceHolder;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKAssert;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVKLOGGER;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_COMPILATION;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.CVK_ERROR_SHADER_MODULE;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.GetCurrentMethodName;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.GetParentMethodName;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.LoadFileToDirectBuffer;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VerifyInRenderThread;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.VkFailed;
import static au.gov.asd.tac.constellation.visual.vulkan.utils.CVKUtils.checkVKret;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS_OR_EQUAL;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SINT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R8_SINT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.VK_WHOLE_SIZE;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkCreateBufferView;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyBufferView;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;
import org.lwjgl.vulkan.VkBufferViewCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandBufferInheritanceInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

/**
 * What resources do icons have
 *  - vs: vertex buffer (icon indexes and bkg colour)
 *  - vs: xyzw buffer (icon positions)
 *  - vs: vertex ubo (camera vars)
 *  - gs: flag buffer (vertex flags)
 *  - gs: geometry ubo (proj mtx and render data)
 *  - fs: atlas texture sampler (
 *  - fs: fragment ubo (hit test flag)
 * 
 */
public class CVKIconsRenderable extends CVKRenderable{
    // Static so we recreate descriptor layouts and shaders for each graph
    private static boolean staticInitialised = false;
    
    private static final int ICON_BITS = 16;
    private static final int ICON_MASK = 0xffff;
    private static final int POSITION_STRIDE = 8 * Float.BYTES;
    private static final int FLAGS_STRIDE = Byte.BYTES;
    public static final int SELECTED_BIT = 1;
    public static final int DIMMED_BIT = 2;
    
    private long hVertexShader = VK_NULL_HANDLE;
    private long hGeometryShader = VK_NULL_HANDLE;
    private long hFragmentShader = VK_NULL_HANDLE;
    private static ByteBuffer vsBytes = null;
    private static ByteBuffer gsBytes = null;
    private static ByteBuffer fsBytes = null;    
    
    private long hDescriptorLayout = VK_NULL_HANDLE; 
    private long hPipelineLayout = VK_NULL_HANDLE; 

    // Resource states. The atlas sampler handle is cached so we know the atlas 
    // state, ie if it doesn't match the one returned by the atlas, we know we
    // need to recreate our descriptors to point to the new one.
    private CVKRenderableResourceState positionBufferState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState vertexFlagsBufferState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState vertexUBOState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState geometryUBOState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState fragmentUBOState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState vertexBuffersState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState commandBuffersState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState descriptorSetsState = CVK_RESOURCE_CLEAN;
    private CVKRenderableResourceState pipelinesState = CVK_RESOURCE_CLEAN;    
    private long hAtlasSampler = VK_NULL_HANDLE;
    private long hAtlasImageView = VK_NULL_HANDLE;    
        
    // Resources recreated with the swap chain (dependent on the image count)    
    private LongBuffer pDescriptorSets = null; 
    private List<Long> pipelines = null;
    private List<CVKCommandBuffer> commandBuffers = null;    
    private List<CVKBuffer> vertexBuffers = null;   
    private List<CVKBuffer> vertexUniformBuffers = null;
    private List<CVKBuffer> geometryUniformBuffers = null;    
    private List<CVKBuffer> fragmentUniformBuffers = null;        
    
    // The UBO staging buffers are a known size so created outside user events
    private CVKBuffer cvkVertexUBStagingBuffer = null;
    private CVKBuffer cvkGeometryUBStagingBuffer = null;
    private CVKBuffer cvkFragmentUBStagingBuffer = null;
    private VertexUniformBufferObject vertexUBO = new VertexUniformBufferObject();
    private GeometryUniformBufferObject geometryUBO = new GeometryUniformBufferObject();
    private FragmentUniformBufferObject fragmentUBO = new FragmentUniformBufferObject();    
    
    // Resources recreated only through user events
    private int vertexCount = 0;
    private CVKBuffer cvkVertexStagingBuffer = null;
    private CVKBuffer cvkPositionStagingBuffer = null;
    private CVKBuffer cvkPositionBuffer = null;    
    private CVKBuffer cvkVertexFlagsStagingBuffer = null;
    private CVKBuffer cvkVertexFlagsBuffer = null;    
    private long hPositionBufferView = VK_NULL_HANDLE;
    private long hVertexFlagsBufferView = VK_NULL_HANDLE;
    
    // The vertex, position and flags staging buffers are used by both the event
    // thread and rendering thread so must be synchronised.
    private ReentrantLock vertexStagingBufferLock = new ReentrantLock();
    private ReentrantLock positionStagingBufferLock = new ReentrantLock();
    private ReentrantLock vertexFlagsStagingBufferLock = new ReentrantLock();
    
    
    // ========================> Debuggering <======================== \\
    
    private void SetPositionBufferState(final CVKRenderableResourceState state) {
        CVKAssert(!(positionBufferState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t positionBufferState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), positionBufferState.name(), state.name(), GetParentMethodName()));        
        positionBufferState = state;
    }
    private void SetVertexFlagsBufferState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t vertexFlagsBufferState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), vertexFlagsBufferState.name(), state.name(), GetParentMethodName()));        
        vertexFlagsBufferState = state;
    }
    private void SetVertexUBOState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t vertexUBOState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), vertexUBOState.name(), state.name(), GetParentMethodName()));        
        vertexUBOState = state;
    }
    private void SetGeometryUBOState(final CVKRenderableResourceState state) {
        CVKAssert(!(geometryUBOState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t geometryUBOState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), geometryUBOState.name(), state.name(), GetParentMethodName()));        
        geometryUBOState = state;
    }
    private void SetFragmentUBOState(final CVKRenderableResourceState state) {
        CVKAssert(!(fragmentUBOState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t fragmentUBOState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), fragmentUBOState.name(), state.name(), GetParentMethodName()));        
        fragmentUBOState = state;
    }
    private void SetVertexBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t vertexBuffersState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), vertexBuffersState.name(), state.name(), GetParentMethodName()));        
        vertexBuffersState = state;
    }
    private void SetCommandBuffersState(final CVKRenderableResourceState state) {
        CVKAssert(!(commandBuffersState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t commandBuffersState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), commandBuffersState.name(), state.name(), GetParentMethodName()));        
        commandBuffersState = state;
    }
    private void SetDescriptorSetsState(final CVKRenderableResourceState state) {
        CVKAssert(!(descriptorSetsState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t descriptorSetsState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), descriptorSetsState.name(), state.name(), GetParentMethodName()));        
        descriptorSetsState = state;
    }
    private void SetPipelinesState(final CVKRenderableResourceState state) {
        CVKAssert(!(pipelinesState == CVK_RESOURCE_NEEDS_REBUILD && state == CVK_RESOURCE_NEEDS_UPDATE));
        CVKLOGGER.info(String.format("%d\t pipelinesState %s -> %s\tSource: %s", 
                parent.GetFrameNumber(), pipelinesState.name(), state.name(), GetParentMethodName()));        
        pipelinesState = state;
    }  
    
    
    // ========================> Classes <======================== \\
    
    private static class Vertex {
        // This looks a little weird for Java, but LWJGL and JOGL both require
        // contiguous memory which is passed to the native GL or VK libraries.        
        private static final int SIZEOF = 4 * Float.BYTES + 4 * Integer.BYTES;
        private static final int OFFSETOF_DATA = 4 * Float.BYTES;
        private static final int OFFSET_BKGCLR = 0;
        private static final int BINDING = 0;
        private Vector4f backgroundIconColour = new Vector4f();
        private Vector4i data = new Vector4i();
        
        public Vertex() {}

        public Vertex(Vector4i inData, Vector4f inColour) {
            data = inData;
            backgroundIconColour = inColour;
        }
        
        public void SetBackgroundIconColour(ConstellationColor colour) {
            backgroundIconColour.a[0] = colour.getRed();
            backgroundIconColour.a[1] = colour.getGreen();
            backgroundIconColour.a[2] = colour.getBlue();
        }
        
        public void SetVertexVisibility(float visibility) {
            backgroundIconColour.a[3] = visibility;
        }
        
        public void SetIconData(int mainIconIndices, int decoratorWestIconIndices, int decoratorEastIconIndices, int vertexIndex) {
            data.set(mainIconIndices, decoratorWestIconIndices, decoratorEastIconIndices, vertexIndex);
        }   
        
        public void CopyTo(ByteBuffer buffer) {
            buffer.putFloat(backgroundIconColour.a[0]);
            buffer.putFloat(backgroundIconColour.a[1]);
            buffer.putFloat(backgroundIconColour.a[2]);
            buffer.putFloat(backgroundIconColour.a[3]);
            buffer.putInt(data.a[0]);
            buffer.putInt(data.a[1]);
            buffer.putInt(data.a[2]);
            buffer.putInt(data.a[3]);              
        }        

        /**
         * A VkVertexInputBindingDescription defines the rate at which data is
         * consumed by vertex shader (per vertex or per instance).  
         * The input rate determines whether to move to the next data entry after
         * each vertex or after each instance.
         * The binding description also defines the vertex stride, the number of
         * bytes that must be stepped from vertex n-1 to vertex n.
         * 
         * @return Binding description for the FPS vertex type
         */
        private static VkVertexInputBindingDescription.Buffer GetBindingDescription() {

            VkVertexInputBindingDescription.Buffer bindingDescription =
                    VkVertexInputBindingDescription.callocStack(1);

            // If we bind multiple vertex buffers with different descriptions
            // this is the index of this description occupies in the array of
            // bound descriptions.
            bindingDescription.binding(BINDING);
            bindingDescription.stride(Vertex.SIZEOF);
            bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

            return bindingDescription;
        }
        
        /**
         * A VkVertexInputAttributeDescription describes each element int the
         * vertex buffer.
         * binding:  matches the binding member of VkVertexInputBindingDescription
         * location: corresponds to the layout(location = #) in the vertex shader
         *           for this element (0 for data, 1 for bkgClr).
         * format:   format the shader will interpret this as.
         * offset:   bytes from the start of the vertex this attribute starts at
         * 
         * @return 
         */
        private static VkVertexInputAttributeDescription.Buffer GetAttributeDescriptions() {

            VkVertexInputAttributeDescription.Buffer attributeDescriptions =
                    VkVertexInputAttributeDescription.callocStack(2);

            // backgroundIconColor
            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(0);
            posDescription.binding(BINDING);
            posDescription.location(0);
            posDescription.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            posDescription.offset(OFFSET_BKGCLR);

            // data
            VkVertexInputAttributeDescription colorDescription = attributeDescriptions.get(1);
            colorDescription.binding(BINDING);
            colorDescription.location(1);
            colorDescription.format(VK_FORMAT_R32G32B32A32_SINT);
            colorDescription.offset(OFFSETOF_DATA);

            return attributeDescriptions.rewind();
        }
    }  
    
    private static class VertexUniformBufferObject {
        private static final int SIZEOF = (16 + 1 + 1 + 1) * Float.BYTES;

        public Matrix44f mvMatrix = new Matrix44f();
        public float morphMix = 0;
        public float visibilityLow = 0;
        public float visibilityHigh = 0;                 
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(mvMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(morphMix);
            buffer.putFloat(visibilityLow);
            buffer.putFloat(visibilityHigh);
        }         
    }
    
    private static class GeometryUniformBufferObject {
        private static final int SIZEOF = 16 * Float.BYTES + 1 * Float.BYTES + 16 * Float.BYTES + 1 * Integer.BYTES;

        public Matrix44f pMatrix = new Matrix44f();
        public float pixelDensity = 0;
        public Matrix44f highlightColor = Matrix44f.identity();
        public int drawHitTest = 0;           
        
        private void CopyTo(ByteBuffer buffer) {
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(pMatrix.get(iRow, iCol));
                }
            }
            buffer.putFloat(pixelDensity);
            for (int iRow = 0; iRow < 4; ++iRow) {
                for (int iCol = 0; iCol < 4; ++iCol) {
                    buffer.putFloat(highlightColor.get(iRow, iCol));
                }
            }            
            buffer.putInt(drawHitTest);
        }         
    }  
    
    private static class FragmentUniformBufferObject {
        private static final int SIZEOF = 1 * Integer.BYTES;

        public int drawHitTest = 0;           
        
        private void CopyTo(ByteBuffer buffer) {
            buffer.putInt(drawHitTest);
        }         
    }    
                      
    
    // ========================> Static init <======================== \\
    
    private static int LoadShaders(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        
        try {
            if (vsBytes == null) {
                vsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.vs.spv");
                if (vsBytes == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to load precompiled CVKIconsRenderable shader: VertexIcon.vs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (gsBytes == null) {
                gsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.gs.spv");
                if (gsBytes == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to load precompiled CVKIconsRenderable shader: VertexIcon.gs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
            
            if (fsBytes == null) {
                fsBytes = LoadFileToDirectBuffer(CVKShaderPlaceHolder.class, "compiled/VertexIcon.fs.spv");
                if (fsBytes == null) {
                    CVKLOGGER.log(Level.SEVERE, "Failed to load precompiled CVKIconsRenderable shader: VertexIcon.fs");
                    return CVK_ERROR_SHADER_COMPILATION;
                }
            }
         
        } catch (IOException e) {
            CVKLOGGER.log(Level.SEVERE, "Failed to compile FPSRenderable shaders: {0}", e.toString());
            ret = CVK_ERROR_SHADER_COMPILATION;
        }
        
        return ret;            
    }  

    public static int StaticInitialise(CVKDevice cvkDevice) {
        int ret = VK_SUCCESS;
        if (!staticInitialised) {
            ret = LoadShaders(cvkDevice);
            if (VkFailed(ret)) { return ret; }            
            staticInitialised = true;
        }
        return ret;
    }   
    
    public static void DestroyStaticResources() {
        if (vsBytes != null) {
            MemoryUtil.memFree(vsBytes);
            vsBytes = null;
        }       
        
        if (gsBytes != null) {
            MemoryUtil.memFree(gsBytes);
            gsBytes = null;
        }

        if (fsBytes != null) {
            MemoryUtil.memFree(fsBytes);
            fsBytes = null;
        }
        
        staticInitialised = false;
    }
        
    
    // ========================> Lifetime <======================== \\
    
    public CVKIconsRenderable(CVKVisualProcessor inParent) {
        parent = inParent;
    }  
    
    private int CreateShaderModules() {
        int ret = VK_SUCCESS;
        
        try{           
            hVertexShader = CVKShaderUtils.createShaderModule(vsBytes, cvkDevice.GetDevice());
            if (hVertexShader == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: VertexIcon.vs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hGeometryShader = CVKShaderUtils.createShaderModule(gsBytes, cvkDevice.GetDevice());
            if (hGeometryShader == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: VertexIcon.gs");
                return CVK_ERROR_SHADER_MODULE;
            }
            hFragmentShader = CVKShaderUtils.createShaderModule(fsBytes, cvkDevice.GetDevice());
            if (hFragmentShader == VK_NULL_HANDLE) {
                CVKLOGGER.log(Level.SEVERE, "Failed to create shader module for: VertexIcon.fs");
                return CVK_ERROR_SHADER_MODULE;
            }
        } catch(Exception ex){
            CVKLOGGER.log(Level.SEVERE, "Failed to create shader module CVKIconsRenderable: {0}", ex.toString());
            ret = CVK_ERROR_SHADER_MODULE;
            return ret;
        }
        
        CVKLOGGER.info(String.format("Shader modules created for CVKIconsRenderable class:\n\tVertex:   0x%016x\n\tGeometry: 0x%016x\n\tFragment: 0x%016x",
                hVertexShader, hGeometryShader, hFragmentShader));
        return ret;
    }   
    
    private void CreateUBOStagingBuffers() {
        cvkVertexUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                    VertexUniformBufferObject.SIZEOF,
                                                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        cvkVertexUBStagingBuffer.DEBUGNAME = "CVKIconsRenderable.CreateUBOStagingBuffers cvkVertexUBStagingBuffer";   
        cvkGeometryUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                      GeometryUniformBufferObject.SIZEOF,
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        cvkGeometryUBStagingBuffer.DEBUGNAME = "CVKIconsRenderable.CreateUBOStagingBuffers cvkGeometryUBStagingBuffer"; 
        cvkFragmentUBStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                      FragmentUniformBufferObject.SIZEOF,
                                                      VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        cvkFragmentUBStagingBuffer.DEBUGNAME = "CVKIconsRenderable.CreateUBOStagingBuffers cvkFragmentUBStagingBuffer";                                
    }
    
    @Override
    public int Initialise(CVKDevice cvkDevice) {
        CVKAssert(cvkDevice != null);
        // Check for double initialisation
        CVKAssert(hVertexShader == VK_NULL_HANDLE);
        CVKAssert(hDescriptorLayout == VK_NULL_HANDLE);
        
        int ret;        
        this.cvkDevice = cvkDevice;
        
        ret = CreateShaderModules();
        if (VkFailed(ret)) { return ret; }             
        
        ret = CreateDescriptorLayout();
        if (VkFailed(ret)) { return ret; }   
        
        ret = CreatePipelineLayout();
        if (VkFailed(ret)) { return ret; }          
                
        CreateUBOStagingBuffers();
        
        return ret;
    }        
    
    private void DestroyStagingBuffers() {        
        if (cvkVertexStagingBuffer != null) {
            try {
                vertexStagingBufferLock.lock();
                cvkVertexStagingBuffer.Destroy();
                cvkVertexStagingBuffer = null;
            } finally {
                vertexStagingBufferLock.unlock();
            }
        }
        if (cvkPositionStagingBuffer != null) {
            try {
                positionStagingBufferLock.lock();
                cvkPositionStagingBuffer.Destroy();
                cvkPositionStagingBuffer = null;
            } finally {
                positionStagingBufferLock.unlock();
            }
        }
        if (cvkVertexFlagsStagingBuffer != null) {
            try {
                vertexFlagsStagingBufferLock.lock();
                cvkVertexFlagsStagingBuffer.Destroy();
                cvkVertexFlagsStagingBuffer = null;
            } finally {
                vertexFlagsStagingBufferLock.unlock();
            }
        }
        
        if (cvkVertexUBStagingBuffer != null) {
            cvkVertexUBStagingBuffer.Destroy();
            cvkVertexUBStagingBuffer = null;
        }
        if (cvkGeometryUBStagingBuffer != null) {
            cvkGeometryUBStagingBuffer.Destroy();
            cvkGeometryUBStagingBuffer = null;
        }        
        if (cvkFragmentUBStagingBuffer != null) {
            cvkFragmentUBStagingBuffer.Destroy();
            cvkFragmentUBStagingBuffer = null;
        }        
    }
    
    @Override
    public void Destroy() {
        DestroyVertexBuffers();
        DestroyPositionBuffer();
        DestroyVertexFlagsBuffer();
        DestroyVertexUniformBuffers();
        DestroyGeometryUniformBuffers();
        DestroyFragmentUniformBuffers();
        DestroyDescriptorSets();
        DestroyDescriptorLayout();
        DestroyCommandBuffers();
        DestroyPipelines();
        DestroyPipelineLayout();
        DestroyCommandBuffers();
        DestroyStagingBuffers();
        
        CVKAssert(vertexBuffers == null);
        CVKAssert(cvkPositionBuffer == null);
        CVKAssert(hPositionBufferView == VK_NULL_HANDLE); 
        CVKAssert(cvkVertexFlagsBuffer == null);
        CVKAssert(hVertexFlagsBufferView == VK_NULL_HANDLE); 
        CVKAssert(vertexUniformBuffers == null);
        CVKAssert(geometryUniformBuffers == null);
        CVKAssert(fragmentUniformBuffers == null); 
        CVKAssert(pDescriptorSets == null);
        CVKAssert(hDescriptorLayout == VK_NULL_HANDLE);  
        CVKAssert(commandBuffers == null);        
        CVKAssert(pipelines == null);
        CVKAssert(hPipelineLayout == VK_NULL_HANDLE);           
    }
    
       
    // ========================> Swap chain <======================== \\
       
    @Override
    public int DestroySwapChainResources() { 
        this.cvkSwapChain = null;
        
        // We only need to recreate these resources if the number of images in 
        // the swapchain changes or if this is the first call after the initial
        // swapchain is created.
        if (pipelines != null && swapChainImageCountChanged) {  
            DestroyVertexBuffers();
            DestroyVertexUniformBuffers();
            DestroyGeometryUniformBuffers();
            DestroyFragmentUniformBuffers();
            DestroyDescriptorSets();
            DestroyCommandBuffers();     
            DestroyPipelines();
            DestroyCommandBuffers();                                  
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
            SetVertexUBOState(CVK_RESOURCE_NEEDS_REBUILD);
            SetGeometryUBOState(CVK_RESOURCE_NEEDS_REBUILD);
            SetFragmentUBOState(CVK_RESOURCE_NEEDS_REBUILD);            
            SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetCommandBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetDescriptorSetsState(CVK_RESOURCE_NEEDS_REBUILD);
            SetPipelinesState(CVK_RESOURCE_NEEDS_REBUILD);
        } else {
            // View frustum and projection matrix likely have changed.  We don't
            // need to rebuild our pipelines as the frustum is set by dynamic
            // state in RecordCommandBuffer
            if (geometryUBOState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetGeometryUBOState(CVK_RESOURCE_NEEDS_UPDATE); 
            }
        }
        
        return ret;
    } 
    
    
    // ========================> Vertex buffers <======================== \\
    
    private int CreateVertexBuffers() {
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
    
        // We can only create vertex buffers if we have something to put in them
        if (vertexCount > 0) {
            int imageCount = cvkSwapChain.GetImageCount();               
            vertexBuffers = new ArrayList<>();
            
            int vertexBufferSizeBytes = CVKIconsRenderable.Vertex.SIZEOF * vertexCount;
            for (int i = 0; i < imageCount; ++i) {   
                CVKBuffer cvkVertexBuffer = CVKBuffer.Create(cvkDevice, 
                                                             vertexBufferSizeBytes,
                                                             VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
                cvkVertexBuffer.DEBUGNAME = String.format("CVKIconsRenderable cvkVertexBuffer %d", i);
                vertexBuffers.add(cvkVertexBuffer);        
            }

            // Populate them with some values
            return UpdateVertexBuffers();
        }
        
        return ret;  
    }    
    
    private int UpdateVertexBuffers() {
        VerifyInRenderThread();
        CVKAssert(cvkVertexStagingBuffer != null);
        CVKAssert(vertexBuffers != null);
        CVKAssert(vertexBuffers.size() > 0);
        CVKAssert(cvkVertexStagingBuffer.GetBufferSize() == vertexBuffers.get(0).GetBufferSize());
        int ret = VK_SUCCESS;
        
        try {
            vertexStagingBufferLock.lock();
            
            List<DEBUG_CVKBufferElementDescriptor> DEBUG_vertexDescriptors = new ArrayList<>();
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("r", Float.TYPE));
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("g", Float.TYPE));
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("b", Float.TYPE));
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("a", Float.TYPE));
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("mainIconIndices", Integer.TYPE));
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("decoratorWestIconIndices", Integer.TYPE));
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("decoratorEastIconIndices", Integer.TYPE));
            DEBUG_vertexDescriptors.add(new DEBUG_CVKBufferElementDescriptor("vertexIndex", Integer.TYPE)); 
            cvkVertexStagingBuffer.DEBUGPRINT(DEBUG_vertexDescriptors);            
            
            for (int i = 0; i < vertexBuffers.size(); ++i) {   
                CVKBuffer cvkVertexBuffer = vertexBuffers.get(i);
                cvkVertexBuffer.CopyFrom(cvkVertexStagingBuffer);
            }
        } finally {
            vertexStagingBufferLock.unlock();
        }     
        
        // Note the staging buffer is not freed as we can simplify the update tasks
        // by just updating it and then copying it over again during ProcessRenderTasks().
        SetVertexBuffersState(CVK_RESOURCE_CLEAN);

        return ret;         
    }  
    
    @Override
    public int GetVertexCount() { return vertexCount; }      
    
    private void DestroyVertexBuffers() {
        if (vertexBuffers != null) {
            vertexBuffers.forEach(el -> {el.Destroy();});
            vertexBuffers.clear();
            vertexBuffers = null;
        }           
    }    
    
    
    // ========================> Texel buffers <======================== \\
    
    private int CreatePositionBuffer() {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkPositionBuffer == null);
        VerifyInRenderThread();        
        int ret = VK_SUCCESS;
                
        cvkPositionBuffer = CVKBuffer.Create(cvkDevice, 
                                              cvkPositionStagingBuffer.GetBufferSize(), 
                                              VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                              VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        cvkPositionBuffer.DEBUGNAME = "CVKIconsRenderable.CreatePositionBuffer cvkPositionBuffer";       
                
        try (MemoryStack stack = stackPush()) {
            // NB: we have already checked VK_FORMAT_R32G32B32A32_SFLOAT can be used as a texel buffer
            // format in CVKDevice.  If the format is changed here we need to check for its support in
            // CVKDevice.
            VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            vkViewInfo.buffer(cvkPositionBuffer.GetBufferHandle());
            vkViewInfo.format(VK_FORMAT_R32G32B32A32_SFLOAT);
            vkViewInfo.offset(0);
            vkViewInfo.range(VK_WHOLE_SIZE);

            LongBuffer pBufferView = stack.mallocLong(1);
            ret = vkCreateBufferView(cvkDevice.GetDevice(), vkViewInfo, null, pBufferView);
            if (VkFailed(ret)) { return ret; }
            hPositionBufferView = pBufferView.get(0);
            
            // Descriptor sets will reference the old buffer view until updated
            SetDescriptorSetsState(pDescriptorSets != null ? CVK_RESOURCE_NEEDS_UPDATE : CVK_RESOURCE_NEEDS_REBUILD);
        }       
        
        return UpdatePositionBuffer();
    }
    
    private int UpdatePositionBuffer() {
        int ret;
        
        try {
            positionStagingBufferLock.lock();
            
            List<DEBUG_CVKBufferElementDescriptor> DEBUG_positionDescriptors = new ArrayList<>();
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("X1", Float.TYPE));
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Y1", Float.TYPE));
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Z1", Float.TYPE));
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Rad1", Float.TYPE));
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("X2", Float.TYPE));
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Y2", Float.TYPE));
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Z2", Float.TYPE));
            DEBUG_positionDescriptors.add(new DEBUG_CVKBufferElementDescriptor("Rad2", Float.TYPE)); 
            cvkPositionStagingBuffer.DEBUGPRINT(DEBUG_positionDescriptors);            
        
            ret = cvkPositionBuffer.CopyFrom(cvkPositionStagingBuffer);             
            SetPositionBufferState(CVK_RESOURCE_CLEAN);                        
        } finally {
            positionStagingBufferLock.unlock();
        }
        
        return ret;                               
    }   
    
    private void DestroyPositionBuffer() {
        if (cvkPositionBuffer != null) {
            cvkPositionBuffer.Destroy();
            cvkPositionBuffer = null;
        }   
        if (hPositionBufferView != VK_NULL_HANDLE) {
            vkDestroyBufferView(cvkDevice.GetDevice(), hPositionBufferView, null);
            hPositionBufferView = VK_NULL_HANDLE;
        }
    }
    
    private int CreateVertexFlagsBuffer() {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkVertexFlagsBuffer == null);
        VerifyInRenderThread();        
        int ret = VK_SUCCESS;
        
        cvkVertexFlagsBuffer = CVKBuffer.Create(cvkDevice, 
                                                cvkVertexFlagsStagingBuffer.GetBufferSize(), 
                                                VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        cvkVertexFlagsBuffer.DEBUGNAME = "CVKIconsRenderable.CreateVertexFlagsBuffer cvkVertexFlagsBuffer";       
                
        try (MemoryStack stack = stackPush()) {
            // NB: we have already checked VK_FORMAT_R8_SINT can be used as a texel buffer
            // format in CVKDevice.  If the format is changed here we need to check for its support in
            // CVKDevice.
            VkBufferViewCreateInfo vkViewInfo = VkBufferViewCreateInfo.callocStack(stack);
            vkViewInfo.sType(VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO);
            vkViewInfo.buffer(cvkVertexFlagsBuffer.GetBufferHandle());
            vkViewInfo.format(VK_FORMAT_R8_SINT);
            vkViewInfo.offset(0);
            vkViewInfo.range(VK_WHOLE_SIZE);

            LongBuffer pBufferView = stack.mallocLong(1);
            ret = vkCreateBufferView(cvkDevice.GetDevice(), vkViewInfo, null, pBufferView);
            if (VkFailed(ret)) { return ret; }
            hVertexFlagsBufferView = pBufferView.get(0);
            
            // Descriptor sets will reference the old buffer view until updated
            SetDescriptorSetsState(pDescriptorSets != null ? CVK_RESOURCE_NEEDS_UPDATE : CVK_RESOURCE_NEEDS_REBUILD);   
        }       
        
        return UpdateVertexFlagsBuffer();
    }
    
    private int UpdateVertexFlagsBuffer() {
        int ret;
        
        try {
            vertexFlagsStagingBufferLock.lock();
        
            ret = cvkVertexFlagsBuffer.CopyFrom(cvkVertexFlagsStagingBuffer);             
            SetVertexFlagsBufferState(CVK_RESOURCE_CLEAN);
        } finally {
            vertexFlagsStagingBufferLock.unlock();
        }
        
        return ret; 
    }
    
    private void DestroyVertexFlagsBuffer() {
        if (cvkVertexFlagsBuffer != null) {
            cvkVertexFlagsBuffer.Destroy();
            cvkVertexFlagsBuffer = null;
        }   
        if (hVertexFlagsBufferView != VK_NULL_HANDLE) {
            vkDestroyBufferView(cvkDevice.GetDevice(), hVertexFlagsBufferView, null);
            hVertexFlagsBufferView = VK_NULL_HANDLE;
        }        
    }
    
    
    // ========================> Uniform buffers <======================== \\
    
    // TODO: make sure these are called when the camera changes etc
    
    private int CreateVertexUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(vertexUniformBuffers == null);
 
        vertexUniformBuffers = new ArrayList<>();
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer vertexUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                             VertexUniformBufferObject.SIZEOF,
                                                             VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                             VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            vertexUniformBuffer.DEBUGNAME = String.format("CVKIconsRenderable vertexUniformBuffer %d", i);   
            vertexUniformBuffers.add(vertexUniformBuffer);                     
        }        
        return UpdateVertexUniformBuffers(stack);
    }
        
    private int UpdateVertexUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkVertexUBStagingBuffer != null);
        CVKAssert(vertexUniformBuffers != null);
        CVKAssert(vertexUniformBuffers.size() > 0);
        
        int ret = VK_SUCCESS;
        
        // Populate the UBO.  This is easy to deal with, but not super efficient
        // as we are effectively staging into the staging buffer below.
        vertexUBO.mvMatrix = parent.getDisplayModelViewMatrix();
        vertexUBO.morphMix = parent.getDisplayCamera().getMix();
        
        // TODO: replace with constants.  In the JOGL version these were in a static var CAMERA that never changed
        vertexUBO.visibilityLow = parent.getDisplayCamera().getVisibilityLow();
        vertexUBO.visibilityHigh = parent.getDisplayCamera().getVisibilityHigh();            

        // Staging buffer so our VBO can be device local (most performant memory)
        final int size = VertexUniformBufferObject.SIZEOF;
        PointerBuffer pData = stack.mallocPointer(1);        
        
        // Map staging buffer into host (CPU) rw memory and copy our UBO into it
        ret = vkMapMemory(cvkDevice.GetDevice(), cvkVertexUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
        if (VkFailed(ret)) { return ret; }
        {
            vertexUBO.CopyTo(pData.getByteBuffer(0, size));
        }
        vkUnmapMemory(cvkDevice.GetDevice(), cvkVertexUBStagingBuffer.GetMemoryBufferHandle());   
     
        // Copy the staging buffer into the uniform buffer on the device
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = vertexUniformBuffers.get(i).CopyFrom(cvkVertexUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }
        
        // We are done, reset the resource state
        SetVertexUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyVertexUniformBuffers() {
        if (vertexUniformBuffers != null) {
            vertexUniformBuffers.forEach(el -> {el.Destroy();});
            vertexUniformBuffers = null;
        }    
    }      
    
    private int CreateGeometryUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(geometryUniformBuffers == null);

        geometryUniformBuffers = new ArrayList<>(); 
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer geometryUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                               GeometryUniformBufferObject.SIZEOF,
                                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            geometryUniformBuffer.DEBUGNAME = String.format("CVKIconsRenderable geometryUniformBuffer %d", i);                                    
            geometryUniformBuffers.add(geometryUniformBuffer);              
        }
        return UpdateGeometryUniformBuffers(stack);
    }
    
    private int UpdateGeometryUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkGeometryUBStagingBuffer != null);
        CVKAssert(geometryUniformBuffers != null);
        CVKAssert(geometryUniformBuffers.size() > 0);        
        
        int ret = VK_SUCCESS;
        
        // Populate the UBO.  This is easy to deal with, but not super efficient
        // as we are effectively staging into the staging buffer below.
        geometryUBO.pMatrix = parent.GetProjectionMatrix();
        geometryUBO.pixelDensity = parent.GetPixelDensity();
        // geometryUBO.highlightColor is set by a task
        geometryUBO.drawHitTest = 0; // TODO: Hydra41 hit test
        
        // Staging buffer so our VBO can be device local (most performant memory)
        final int size = GeometryUniformBufferObject.SIZEOF;
        PointerBuffer pData = stack.mallocPointer(1);        
        
        // Map staging buffer into host (CPU) rw memory and copy our UBO into it
        ret = vkMapMemory(cvkDevice.GetDevice(), cvkGeometryUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
        if (VkFailed(ret)) { return ret; }
        {
            geometryUBO.CopyTo(pData.getByteBuffer(0, size));
        }
        vkUnmapMemory(cvkDevice.GetDevice(), cvkGeometryUBStagingBuffer.GetMemoryBufferHandle());   
     
        // Copy the staging buffer into the uniform buffer on the device
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = geometryUniformBuffers.get(i).CopyFrom(cvkGeometryUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }       
                    
        // We are done, reset the resource state
        SetGeometryUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyGeometryUniformBuffers() {
        if (geometryUniformBuffers != null) {
            geometryUniformBuffers.forEach(el -> {el.Destroy();});
            geometryUniformBuffers = null;
        }                
    }      
    
    private int CreateFragmentUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(fragmentUniformBuffers == null);

        fragmentUniformBuffers = new ArrayList<>();
        for (int i = 0; i < cvkSwapChain.GetImageCount(); ++i) {   
            CVKBuffer fragmentUniformBuffer = CVKBuffer.Create(cvkDevice, 
                                                               FragmentUniformBufferObject.SIZEOF,
                                                               VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                                                               VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
            fragmentUniformBuffer.DEBUGNAME = String.format("CVKIconsRenderable fragmentUniformBuffer %d", i);                                    
            fragmentUniformBuffers.add(fragmentUniformBuffer);              
        }
        return UpdateFragmentUniformBuffers(stack);
    }
    
    private int UpdateFragmentUniformBuffers(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkGeometryUBStagingBuffer != null);
        CVKAssert(fragmentUniformBuffers != null);
        CVKAssert(fragmentUniformBuffers.size() > 0);  
        
        int ret = VK_SUCCESS;
     
        fragmentUBO.drawHitTest = 0; // TODO: Hydra41 hit test
        
        // Staging buffer so our VBO can be device local (most performant memory)
        final int size = FragmentUniformBufferObject.SIZEOF;
        PointerBuffer pData = stack.mallocPointer(1);        
        
        // Map staging buffer into host (CPU) rw memory and copy our UBO into it
        ret = vkMapMemory(cvkDevice.GetDevice(), cvkFragmentUBStagingBuffer.GetMemoryBufferHandle(), 0, size, 0, pData);
        if (VkFailed(ret)) { return ret; }
        {
            fragmentUBO.CopyTo(pData.getByteBuffer(0, size));
        }
        vkUnmapMemory(cvkDevice.GetDevice(), cvkFragmentUBStagingBuffer.GetMemoryBufferHandle());   
     
        // Copy the staging buffer into the uniform buffer on the device
        final int imageCount = cvkSwapChain.GetImageCount(); 
        for (int i = 0; i < imageCount; ++i) {   
            ret = fragmentUniformBuffers.get(i).CopyFrom(cvkFragmentUBStagingBuffer);   
            if (VkFailed(ret)) { return ret; }
        }       
                    
        // We are done, reset the resource state        
        SetFragmentUBOState(CVK_RESOURCE_CLEAN);

        return ret;
    }  
    
    private void DestroyFragmentUniformBuffers() {
        if (fragmentUniformBuffers != null) {
            fragmentUniformBuffers.forEach(el -> {el.Destroy();});
            fragmentUniformBuffers = null;
        }           
    }      
    
    
    // ========================> Command buffers <======================== \\
    
    public int CreateCommandBuffers(){
        CVKAssert(cvkSwapChain != null);
        
        int ret = VK_SUCCESS;
        int imageCount = cvkSwapChain.GetImageCount();
        
        commandBuffers = new ArrayList<>(imageCount);

        for (int i = 0; i < imageCount; ++i) {
            CVKCommandBuffer buffer = CVKCommandBuffer.Create(cvkDevice, VK_COMMAND_BUFFER_LEVEL_SECONDARY);
            buffer.DEBUGNAME = String.format("CVKIconsRenderable %d", i);
            commandBuffers.add(buffer);
        }
        
        SetCommandBuffersState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }   
    
    @Override
    public VkCommandBuffer GetCommandBuffer(int imageIndex) {
        return commandBuffers.get(imageIndex).GetVKCommandBuffer(); 
    }       
    
    @Override
    public int RecordCommandBuffer(VkCommandBufferInheritanceInfo inheritanceInfo, int imageIndex){
        VerifyInRenderThread();
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkDevice.GetCommandPoolHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain != null);
                
        int ret;     
        try (MemoryStack stack = stackPush()) {
 
            // Fill
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.pNext(0);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            beginInfo.pInheritanceInfo(inheritanceInfo);             

            VkCommandBuffer commandBuffer = GetCommandBuffer(imageIndex);
            ret = vkBeginCommandBuffer(commandBuffer, beginInfo);
            checkVKret(ret);
            
	    // Set the dynamic viewport and scissor
            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(cvkSwapChain.GetWidth());
            viewport.height(cvkSwapChain.GetHeight());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(cvkDevice.GetCurrentSurfaceExtent());

            vkCmdSetViewport(commandBuffer, 0, viewport);
            vkCmdSetScissor(commandBuffer, 0, scissor);
            
            vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelines.get(imageIndex));

            LongBuffer pVertexBuffers = stack.longs(vertexBuffers.get(imageIndex).GetBufferHandle());
            LongBuffer offsets = stack.longs(0);

            // Bind verts
            vkCmdBindVertexBuffers(commandBuffer, 0, pVertexBuffers, offsets);

            // Bind descriptors
            vkCmdBindDescriptorSets(commandBuffer, 
                                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                                    hPipelineLayout, 
                                    0, 
                                    stack.longs(pDescriptorSets.get(imageIndex)), 
                                    null);            
            
            vkCmdDraw(commandBuffer,
                      GetVertexCount(),  //number of verts == number of digits
                      1,  //no instancing, but we must draw at least 1 point
                      0,  //first vert index
                      0); //first instance index (N/A)                         
            ret = vkEndCommandBuffer(commandBuffer);
            checkVKret(ret);
        }
        
        return ret;
    }       
    
    private void DestroyCommandBuffers() {         
        if (null != commandBuffers) {
            commandBuffers.forEach(el -> {el.Destroy();});
            commandBuffers.clear();
            commandBuffers = null;
        }      
    }        
    
        
    // ========================> Descriptors <======================== \\
    
    private int CreateDescriptorLayout() {
        int ret;
        
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(6, stack);

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
            
            // 3: Geometry isamplerBuffer (vertex flags buffer)
            VkDescriptorSetLayoutBinding geometrySamplerDSLB = bindings.get(3);
            geometrySamplerDSLB.binding(3);
            geometrySamplerDSLB.descriptorCount(1);
            geometrySamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
            geometrySamplerDSLB.pImmutableSamplers(null);
            geometrySamplerDSLB.stageFlags(VK_SHADER_STAGE_GEOMETRY_BIT);               

            // 4: Fragment sampler2Darray (atlas)
            VkDescriptorSetLayoutBinding fragmentSamplerDSLB = bindings.get(4);
            fragmentSamplerDSLB.binding(4);
            fragmentSamplerDSLB.descriptorCount(1);
            fragmentSamplerDSLB.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            fragmentSamplerDSLB.pImmutableSamplers(null);
            fragmentSamplerDSLB.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            
            // 5: Fragment uniform buffer
            VkDescriptorSetLayoutBinding fragmentUBDSLB = bindings.get(5);
            fragmentUBDSLB.binding(5);
            fragmentUBDSLB.descriptorCount(1);
            fragmentUBDSLB.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            fragmentUBDSLB.pImmutableSamplers(null);
            fragmentUBDSLB.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);                  

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

            ret = vkCreateDescriptorSetLayout(cvkDevice.GetDevice(), layoutInfo, null, pDescriptorSetLayout);
            if (VkSucceeded(ret)) {
                hDescriptorLayout = pDescriptorSetLayout.get(0);
            }
        }        
        return ret;
    }      
    
    private void DestroyDescriptorLayout() {
        vkDestroyDescriptorSetLayout(cvkDevice.GetDevice(), hDescriptorLayout, null);
        hDescriptorLayout = VK_NULL_HANDLE;
    }
    
    private int CreateDescriptorSets(MemoryStack stack) {
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain != null);
        
        int ret;    

        // The same layout is used for each descriptor set (each descriptor set is
        // identical but allow the GPU and CPU to desynchronise.
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
        ret = vkAllocateDescriptorSets(cvkDevice.GetDevice(), allocInfo, pDescriptorSets);
        if (VkFailed(ret)) { return ret; }
        
        return UpdateDescriptorSets(stack);
    }
    
    // TODO_TT: do we gain anything by having buffered UBOs?
    private int UpdateDescriptorSets(MemoryStack stack) {
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(pDescriptorSets != null);
        CVKAssert(pDescriptorSets.capacity() > 0);
        CVKAssert(hPositionBufferView != VK_NULL_HANDLE);
        CVKAssert(hVertexFlagsBufferView != VK_NULL_HANDLE);
        CVKAssert(vertexUniformBuffers != null);
        CVKAssert(vertexUniformBuffers.size() > 0);
        CVKAssert(geometryUniformBuffers != null);
        CVKAssert(geometryUniformBuffers.size() > 0);
        CVKAssert(fragmentUniformBuffers != null);
        CVKAssert(fragmentUniformBuffers.size() > 0);        
        
        int ret = VK_SUCCESS;
     
        final int imageCount = cvkSwapChain.GetImageCount();
        
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
        vertexUniformBufferInfo.range(VertexUniformBufferObject.SIZEOF);        
        
        // Struct for texel buffer (positions) used by VertexIcon.vs
        VkDescriptorBufferInfo.Buffer positionsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        positionsTexelBufferInfo.buffer(cvkPositionBuffer.GetBufferHandle());
        positionsTexelBufferInfo.offset(0);
        positionsTexelBufferInfo.range(cvkPositionBuffer.GetBufferSize());               

        // Struct for the uniform buffer used by VertexIcon.gs
        VkDescriptorBufferInfo.Buffer geometryUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // geometryBufferInfo.buffer is set per imageIndex
        geometryUniformBufferInfo.offset(0);
        geometryUniformBufferInfo.range(GeometryUniformBufferObject.SIZEOF);      
        
        // Struct for texel buffer (vertex flags) used by VertexIcon.gs
        VkDescriptorBufferInfo.Buffer vertexFlagsTexelBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        vertexFlagsTexelBufferInfo.buffer(cvkVertexFlagsBuffer.GetBufferHandle());
        vertexFlagsTexelBufferInfo.offset(0);
        vertexFlagsTexelBufferInfo.range(cvkVertexFlagsBuffer.GetBufferSize());            

        // Struct for the size of the image sampler (atlas) used by VertexIcon.fs
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(parent.GetTextureAtlas().GetAtlasImageViewHandle());
        imageInfo.sampler(parent.GetTextureAtlas().GetAtlasSamplerHandle());
        
        // Struct for the uniform buffer used by VertexIcon.fs
        VkDescriptorBufferInfo.Buffer fragmentUniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
        // fragmentUniformBufferInfo.buffer is set per imageIndex
        fragmentUniformBufferInfo.offset(0);
        fragmentUniformBufferInfo.range(FragmentUniformBufferObject.SIZEOF);          

        // We need 6 write descriptors, 3 for uniform buffers, 2 for texel buffers and 1 for texture sampler                       
        VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(6, stack);

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
        
        // Geometry texel buffer (vertex flags)
        VkWriteDescriptorSet vertexFlagsTBDescriptorWrite = descriptorWrites.get(3);
        vertexFlagsTBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        vertexFlagsTBDescriptorWrite.dstBinding(3);
        vertexFlagsTBDescriptorWrite.dstArrayElement(0);
        vertexFlagsTBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER);
        vertexFlagsTBDescriptorWrite.descriptorCount(1);
        vertexFlagsTBDescriptorWrite.pBufferInfo(vertexFlagsTexelBufferInfo);       
        vertexFlagsTBDescriptorWrite.pTexelBufferView(stack.longs(hVertexFlagsBufferView));

        // Fragment image (atlas) sampler
        VkWriteDescriptorSet atlasSamplerDescriptorWrite = descriptorWrites.get(4);
        atlasSamplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        atlasSamplerDescriptorWrite.dstBinding(4);
        atlasSamplerDescriptorWrite.dstArrayElement(0);
        atlasSamplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
        atlasSamplerDescriptorWrite.descriptorCount(1);
        atlasSamplerDescriptorWrite.pImageInfo(imageInfo);   
        
        // Fragment uniform buffer
        VkWriteDescriptorSet fragmentUBDescriptorWrite = descriptorWrites.get(5);
        fragmentUBDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        fragmentUBDescriptorWrite.dstBinding(5);
        fragmentUBDescriptorWrite.dstArrayElement(0);
        fragmentUBDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
        fragmentUBDescriptorWrite.descriptorCount(1);
        fragmentUBDescriptorWrite.pBufferInfo(fragmentUniformBufferInfo);   
                

        for (int i = 0; i < imageCount; ++i) {                        
            // Update the buffered resource buffers
            vertexUniformBufferInfo.buffer(vertexUniformBuffers.get(i).GetBufferHandle());
            geometryUniformBufferInfo.buffer(geometryUniformBuffers.get(i).GetBufferHandle());
            fragmentUniformBufferInfo.buffer(fragmentUniformBuffers.get(i).GetBufferHandle());
                    
            // Set the descriptor set we're updating in each write struct
            long descriptorSet = pDescriptorSets.get(i);
            descriptorWrites.forEach(el -> {el.dstSet(descriptorSet);});

            // Update the descriptors with a write and no copy
            vkUpdateDescriptorSets(cvkDevice.GetDevice(), descriptorWrites, null);
        }
        
        // Cache atlas handles so we know when to recreate descriptors
        hAtlasSampler = parent.GetTextureAtlas().GetAtlasSamplerHandle();
        hAtlasImageView = parent.GetTextureAtlas().GetAtlasImageViewHandle();            
        
        SetDescriptorSetsState(CVK_RESOURCE_CLEAN);
        
        return ret;
    }
        
    private int DestroyDescriptorSets() {
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkDescriptorPool.GetDescriptorPoolHandle() != VK_NULL_HANDLE);
        int ret = VK_SUCCESS;
        
        if (pDescriptorSets != null) {
            CVKLOGGER.info(String.format("CVKIconsRenderable returning %d descriptor sets to the pool", pDescriptorSets.capacity()));
            
            // After calling vkFreeDescriptorSets, all descriptor sets in pDescriptorSets are invalid.
            ret = vkFreeDescriptorSets(cvkDevice.GetDevice(), cvkDescriptorPool.GetDescriptorPoolHandle(), pDescriptorSets);
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
        // VertexIcon.vs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // VertexIcon.gs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER];
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER];
        
        // VertexIcon.fs
        ++perImageReqs.poolDescriptorTypeCounts[VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER];
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
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(hDescriptorLayout != VK_NULL_HANDLE);
               
        int ret;       
        try (MemoryStack stack = stackPush()) {           
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(hDescriptorLayout));
            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            ret = vkCreatePipelineLayout(cvkDevice.GetDevice(), pipelineLayoutInfo, null, pPipelineLayout);
            if (VkFailed(ret)) { return ret; }
            hPipelineLayout = pPipelineLayout.get(0);
            CVKAssert(hPipelineLayout != VK_NULL_HANDLE);                
        }        
        return ret;        
    }    
    
    private int CreatePipelines() {
        CVKAssert(hPipelineLayout != VK_NULL_HANDLE);
        CVKAssert(cvkDevice != null);
        CVKAssert(cvkDevice.GetDevice() != null);
        CVKAssert(cvkSwapChain != null);
        CVKAssert(cvkDescriptorPool != null);
        CVKAssert(cvkSwapChain.GetSwapChainHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkSwapChain.GetRenderPassHandle() != VK_NULL_HANDLE);
        CVKAssert(cvkDescriptorPool.GetDescriptorPoolHandle() != VK_NULL_HANDLE);
        CVKAssert(hVertexShader != VK_NULL_HANDLE);
        CVKAssert(hGeometryShader != VK_NULL_HANDLE);
        CVKAssert(hFragmentShader != VK_NULL_HANDLE);        
        CVKAssert(cvkSwapChain.GetWidth() > 0);
        CVKAssert(cvkSwapChain.GetHeight() > 0);
               
        final int imageCount = cvkSwapChain.GetImageCount();                
        int ret = VK_SUCCESS;
        try (MemoryStack stack = stackPush()) {                 
            // A complete pipeline for each swapchain image.  Wasteful?
            pipelines = new ArrayList<>(imageCount);            
            for (int i = 0; i < imageCount; ++i) {                              
                // prepare vertex attributes

                //From the GL FPSBatcher and FPSRenderable and shaders:
                // 1 vertex per digit.
                // Vert inputs:
                // int[2] data {icon indexes (encoded to int), digit index * 4)
                // float[4] backgroundIconColor
                // Vert outputs:
                // flat out ivec2 gData; this is data passed through
                // out mat4 gBackgroundIconColor; backgroundIconColor in a 4x4 matrix
                // flat out float gRadius; 1 if visible, -1 otherwise
                // gl_Position = mvMatrix * vec4(digitPosition, 1); where digitPosition is (digit index * 4, 0, 0)

                // A bunch of uniforms:
                // SimpleIcon.vs:
                // uniform mat4 mvMatrix;
                // uniform float visibilityLow;
                // uniform float visibilityHigh;
                // uniform float offset;

                // SimpleIcon.gs:
                // Input:
                // uniform mat4 pMatrix;
                // uniform float pixelDensity;
                // uniform float pScale;     
                // Ouput:
                // flat out mat4 iconColor;
                // noperspective centroid out vec3 textureCoords;
                // layout(triangle_strip, max_vertices=28) out;     


 
                final ByteBuffer entryPoint = stack.UTF8("main");

                VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(3, stack);

                VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
                vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
                vertShaderStageInfo.module(hVertexShader);
                vertShaderStageInfo.pName(entryPoint);

                VkPipelineShaderStageCreateInfo geomShaderStageInfo = shaderStages.get(1);
                geomShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                geomShaderStageInfo.stage(VK_SHADER_STAGE_GEOMETRY_BIT);
                geomShaderStageInfo.module(hGeometryShader);
                geomShaderStageInfo.pName(entryPoint);            

                VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(2);
                fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
                fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
                fragShaderStageInfo.module(hFragmentShader);
                fragShaderStageInfo.pName(entryPoint);

                // ===> VERTEX STAGE <===
                VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
                vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
                vertexInputInfo.pVertexBindingDescriptions(Vertex.GetBindingDescription());
                vertexInputInfo.pVertexAttributeDescriptions(Vertex.GetAttributeDescriptions());

                // ===> ASSEMBLY STAGE <===
                // Each point becomes two or more triangles in the geometry shader, but our input is a point list
                VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
                inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
                inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_POINT_LIST);
                inputAssembly.primitiveRestartEnable(false);

                // ===> VIEWPORT & SCISSOR
                VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
                viewport.x(0.0f);
                viewport.y(0.0f);
                viewport.width(cvkSwapChain.GetWidth());
                viewport.height(cvkSwapChain.GetHeight());
                viewport.minDepth(0.0f);
                viewport.maxDepth(1.0f);

                VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
                scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
                scissor.extent(cvkSwapChain.GetExtent());

                VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
                viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
                viewportState.pViewports(viewport);
                viewportState.pScissors(scissor);

                // ===> RASTERIZATION STAGE <===
                VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
                rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
                rasterizer.depthClampEnable(false);
                rasterizer.rasterizerDiscardEnable(false);
                rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
                rasterizer.lineWidth(1.0f);
                rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
                rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
                rasterizer.depthBiasEnable(false);

                // ===> MULTISAMPLING <===
                VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
                multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
                multisampling.sampleShadingEnable(false);
                multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
                
                // ===> DEPTH <===
                VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
                depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
                depthStencil.depthTestEnable(true);
                depthStencil.depthWriteEnable(true);
                depthStencil.depthCompareOp(VK_COMPARE_OP_LESS_OR_EQUAL);
                depthStencil.depthBoundsTestEnable(false);
//                depthStencil.minDepthBounds(0.0f);
//                depthStencil.maxDepthBounds(1.0f);
                depthStencil.stencilTestEnable(false);                       

                // ===> COLOR BLENDING <===
                VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.callocStack(1, stack);
                colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);
                colorBlendAttachment.blendEnable(false);

                VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
                colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
                colorBlending.logicOpEnable(false);
                colorBlending.logicOp(VK_LOGIC_OP_COPY);
                colorBlending.pAttachments(colorBlendAttachment);
                colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

                // ===> DYNAMIC PROPERTIES CREATION <===
                IntBuffer pDynamicStates = memAllocInt(2);
                pDynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT);
                pDynamicStates.put(VK_DYNAMIC_STATE_SCISSOR);
                pDynamicStates.flip();
                VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.callocStack(stack);
                dynamicState.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
                dynamicState.pDynamicStates(pDynamicStates);
                                
                // ===> PIPELINE CREATION <===
                VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
                pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
                pipelineInfo.pStages(shaderStages);
                pipelineInfo.pVertexInputState(vertexInputInfo);
                pipelineInfo.pInputAssemblyState(inputAssembly);
                pipelineInfo.pViewportState(viewportState);
                pipelineInfo.pRasterizationState(rasterizer);
                pipelineInfo.pMultisampleState(multisampling);
                pipelineInfo.pDepthStencilState(depthStencil);
                pipelineInfo.pColorBlendState(colorBlending);
                pipelineInfo.layout(hPipelineLayout);
                pipelineInfo.renderPass(cvkSwapChain.GetRenderPassHandle());
                pipelineInfo.subpass(0);
                pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
                pipelineInfo.basePipelineIndex(-1);
                pipelineInfo.pDynamicState(dynamicState);

                LongBuffer pGraphicsPipeline = stack.mallocLong(1);
                ret = vkCreateGraphicsPipelines(cvkDevice.GetDevice(), 
                                                VK_NULL_HANDLE, 
                                                pipelineInfo, 
                                                null, 
                                                pGraphicsPipeline);
                if (VkFailed(ret)) { return ret; }
                CVKAssert(pGraphicsPipeline.get(0) != VK_NULL_HANDLE);  
                pipelines.add(pGraphicsPipeline.get(0));                      
            }
        }
        
        SetPipelinesState(CVK_RESOURCE_CLEAN);
        CVKLOGGER.log(Level.INFO, "Graphics Pipeline created for CVKIconsRenderable class.");
        return ret;
    }
    
    private void DestroyPipelines() {
        if (pipelines != null) {
            for (int i = 0; i < pipelines.size(); ++i) {
                vkDestroyPipeline(cvkDevice.GetDevice(), pipelines.get(i), null);
                pipelines.set(i, VK_NULL_HANDLE);
            }
            pipelines.clear();
            pipelines = null;
        }        
    }
    
    private void DestroyPipelineLayout() {
        if (hPipelineLayout != VK_NULL_HANDLE) {
            vkDestroyPipelineLayout(cvkDevice.GetDevice(), hPipelineLayout, null);
            hPipelineLayout = VK_NULL_HANDLE;
        }
    }      


    // ========================> Display <======================== \\
    
    @Override
    public boolean NeedsDisplayUpdate() {         
        return vertexCount > 0 &&
               (positionBufferState != CVK_RESOURCE_CLEAN ||
                vertexFlagsBufferState != CVK_RESOURCE_CLEAN ||
                vertexUBOState != CVK_RESOURCE_CLEAN ||
                geometryUBOState != CVK_RESOURCE_CLEAN ||
                fragmentUBOState != CVK_RESOURCE_CLEAN ||                
                vertexBuffersState != CVK_RESOURCE_CLEAN ||
                commandBuffersState != CVK_RESOURCE_CLEAN ||
                descriptorSetsState != CVK_RESOURCE_CLEAN ||
                pipelinesState != CVK_RESOURCE_CLEAN ||                
                hAtlasSampler != parent.GetTextureAtlas().GetAtlasSamplerHandle() ||
                hAtlasImageView != parent.GetTextureAtlas().GetAtlasImageViewHandle() ); 
    }
    
    @Override
    public int DisplayUpdate() { 
        int ret = VK_SUCCESS;
        VerifyInRenderThread();
        
        if (hAtlasSampler != parent.GetTextureAtlas().GetAtlasSamplerHandle() ||
            hAtlasImageView != parent.GetTextureAtlas().GetAtlasImageViewHandle()) {
            if (descriptorSetsState != CVK_RESOURCE_NEEDS_REBUILD) {
                descriptorSetsState = CVK_RESOURCE_NEEDS_UPDATE;
            }
        }
                  
        try (MemoryStack stack = stackPush()) {
            // Update vertex buffers
            if (vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
                DestroyVertexBuffers();
                ret = CreateVertexBuffers();
                if (VkFailed(ret)) { return ret; }         
            } else if (vertexBuffersState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = UpdateVertexBuffers();
                if (VkFailed(ret)) { return ret; }           
            }

            // Update position buffer
            if (positionBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                DestroyPositionBuffer();
                ret = CreatePositionBuffer();
                if (VkFailed(ret)) { return ret; }            
            } else if (positionBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = UpdatePositionBuffer();
                if (VkFailed(ret)) { return ret; }            
            }

            // Update vertex flags buffer
            if (vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                DestroyVertexFlagsBuffer();
                ret = CreateVertexFlagsBuffer();
                if (VkFailed(ret)) { return ret; }            
            } else if (vertexFlagsBufferState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = UpdateVertexFlagsBuffer();
                if (VkFailed(ret)) { return ret; }            
            }                  
        
            // Vertex uniform buffer (camera guff)
            if (vertexUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateVertexUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }
            } else if (vertexUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateVertexUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }               
            }

            // Geometry uniform buffer (projection, highlight colour and hit test)
            if (geometryUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateGeometryUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }
            } else if (geometryUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateGeometryUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }               
            }

            // Update fragment uniform buffer (hit test)
            if (fragmentUBOState == CVK_RESOURCE_NEEDS_REBUILD) {
                ret = CreateFragmentUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }
            } else if (fragmentUBOState == CVK_RESOURCE_NEEDS_UPDATE) {
                ret = UpdateFragmentUniformBuffers(stack);
                if (VkFailed(ret)) { return ret; }               
            }                      

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
                ret = CreatePipelines();
                if (VkFailed(ret)) { return ret; }  
            }                                            
        }                                     
        
        return ret;
    }        
    
    
    // ========================> Tasks <======================== \\    
          
    private void RebuildIconStagingBuffers(final VisualAccess access, final int newVertexCount) {
        // Note this will be called from the visual processer thread, not the render thread
        try {
            // Vertices are modified by the event thread
            vertexStagingBufferLock.lock(); 
            positionStagingBufferLock.lock();
            
            // Destroy old staging buffer if it exists
            if (cvkVertexStagingBuffer != null) {
                cvkVertexStagingBuffer.Destroy();
                cvkVertexStagingBuffer = null;
            }       
            if (cvkPositionStagingBuffer != null) {
                cvkPositionStagingBuffer.Destroy();
                cvkPositionStagingBuffer = null;
            }            
            
            if (newVertexCount > 0) {
                int vertexBufferSizeBytes = CVKIconsRenderable.Vertex.SIZEOF * newVertexCount;
                cvkVertexStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                          vertexBufferSizeBytes, 
                                                          VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                          VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkVertexStagingBuffer.DEBUGNAME = "CVKIconsRenderable.TaskCreateIcons cvkVertexStagingBuffer";
                
                int positionBufferSizeBytes = POSITION_STRIDE * newVertexCount;
                cvkPositionStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                            positionBufferSizeBytes, 
                                                            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkPositionStagingBuffer.DEBUGNAME = "CVKIconsRenderable.TaskCreateIcons cvkPositionBuffer";
                
                ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartWrite(0, vertexBufferSizeBytes);
                ByteBuffer pPositionMemory = cvkPositionStagingBuffer.StartWrite(0, positionBufferSizeBytes);
                CVKIconsRenderable.Vertex vertex = new CVKIconsRenderable.Vertex();
                for (int pos = 0; pos < newVertexCount; pos++) {
                    SetColorInfo(pos, vertex, access);
                    SetIconIndexes(pos, vertex, access);
                    vertex.CopyTo(pVertexMemory);
                    SetVertexPosition(pos, pPositionMemory, access);
                }
                int vertMemPos = pVertexMemory.position();
                CVKAssert(vertMemPos == vertexBufferSizeBytes);
                cvkVertexStagingBuffer.EndWrite();
                pVertexMemory = null; // now unmapped, do not use
                int positionMemPos = pPositionMemory.position();
                CVKAssert(positionMemPos == positionBufferSizeBytes);
                cvkPositionStagingBuffer.EndWrite();
                pPositionMemory = null; // now unmapped, do not use
            }
        } finally {
            vertexStagingBufferLock.unlock();
            positionStagingBufferLock.unlock();
        }        
    }
    
    public CVKRenderableUpdateTask TaskRebuildIcons(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        final int newVertexCount = access.getVertexCount();
        CVKLOGGER.info(String.format("TaskRebuildIcons frame %d: %d verts", parent.GetFrameNumber(), access.getVertexCount()));
        RebuildIconStagingBuffers(access, newVertexCount);
        RebuildVertexFlagsStagingBuffers(access, newVertexCount);
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            // We can't update the position buffer here as it is needed to render each image
            // in the swap chain.  If we recreate it for image 1 it will be likely be in
            // flight for presenting image 0.  The shared resource recreation path is
            // synchronised for all images so we need to do it there.
            SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
            SetPositionBufferState(CVK_RESOURCE_NEEDS_REBUILD);
            SetVertexFlagsBufferState(CVK_RESOURCE_NEEDS_REBUILD);                        
            
            vertexCount = newVertexCount;
        };
    }    
    
    public CVKRenderableUpdateTask TaskUpdateIcons(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        // If this fires investigate why we didn't get a rebuild task first
        final int newVertexCount = access.getVertexCount();
        CVKLOGGER.info(String.format("TaskUpdateIcons frame %d: %d verts", parent.GetFrameNumber(), access.getVertexCount()));
        CVKAssert(vertexCount == newVertexCount); //REMOVE AFTER TESTING
        
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if the vertex count has somehow changed.
        final boolean rebuildRequired = cvkVertexStagingBuffer == null || 
                                        vertexCount != newVertexCount ||
                                        hPositionBufferView == VK_NULL_HANDLE;
        if (rebuildRequired) {
            RebuildIconStagingBuffers(access, newVertexCount);
            RebuildVertexFlagsStagingBuffers(access, newVertexCount);
        }
        
        try {
            vertexStagingBufferLock.lock();

            // We map the whole range as GraphVisualAccess applies any per vertex change to all 
            // vertices in the accessGraph so the change will contain all vertices anyway.
            ByteBuffer pVertexMemory = cvkVertexStagingBuffer.StartWrite(0, (int)cvkVertexStagingBuffer.GetBufferSize());
            final int numChanges = change.getSize();
            for (int i = 0; i < numChanges; ++i) {
                int pos = change.getElement(i);
                Vector4i iconIndexes = MakeIconIndexes(pos, access);
                final int offset = (Vertex.SIZEOF * pos) + Vertex.OFFSETOF_DATA;
                pVertexMemory.position(offset);
                pVertexMemory.putInt(iconIndexes.getX());
                pVertexMemory.putInt(iconIndexes.getY());
                pVertexMemory.putInt(iconIndexes.getZ());
                pVertexMemory.putInt(iconIndexes.getW());
            }
        } finally {
            vertexStagingBufferLock.unlock();
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (rebuildRequired) {
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_REBUILD);
                SetPositionBufferState(CVK_RESOURCE_NEEDS_REBUILD);
                SetVertexFlagsBufferState(CVK_RESOURCE_NEEDS_REBUILD);
                
                vertexCount = newVertexCount;
            } else if (vertexBuffersState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };        
    }  
    
    private void RebuildVertexFlagsStagingBuffers(final VisualAccess access, final int newVertexCount) {
        try {
            // Vertices are modified by the event thread
            vertexFlagsStagingBufferLock.lock(); 
            
            // Destroy old staging buffer if it exists
            if (cvkVertexFlagsStagingBuffer != null) {
                cvkVertexFlagsStagingBuffer.Destroy();
                cvkVertexFlagsStagingBuffer = null;
            }       
            
            if (newVertexCount > 0) {
                int vertexFlagsBufferSizeBytes = newVertexCount * FLAGS_STRIDE;
                cvkVertexFlagsStagingBuffer = CVKBuffer.Create(cvkDevice, 
                                                               vertexFlagsBufferSizeBytes, 
                                                               VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                                                               VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                cvkVertexFlagsStagingBuffer.DEBUGNAME = "CVKIconsRenderable.RebuildVertexFlagsStagingBuffers cvkVertexFlagsStagingBuffer";
                
                ByteBuffer pVertexFlagsMemory = cvkVertexFlagsStagingBuffer.StartWrite(0, vertexFlagsBufferSizeBytes);
                for (int pos = 0; pos < newVertexCount; pos++) {
                    SetVertexFlags(pos, 0, pVertexFlagsMemory, access);
                }
                cvkVertexFlagsStagingBuffer.EndWrite();
                pVertexFlagsMemory = null; // now unmapped, do not use           
            }
        } finally {
            vertexFlagsStagingBufferLock.unlock();
        }         
    }
    
    public CVKRenderableUpdateTask TaskRebuildVertexFlags(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        final int newVertexCount = access.getVertexCount();
//        CVKAssert(newVertexCount == vertexCount);
        CVKLOGGER.info(String.format("TaskRebuildVertexFlags frame %d: %d verts", parent.GetFrameNumber(), access.getVertexCount()));
        RebuildVertexFlagsStagingBuffers(access, newVertexCount);
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {            
            SetVertexFlagsBufferState(CVK_RESOURCE_NEEDS_REBUILD);
        };     
    }
    
    public CVKRenderableUpdateTask TaskUpdateVertexFlags(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        // If this fires investigate why we didn't get a rebuild task first
        CVKLOGGER.info(String.format("TaskUpdateVertexFlags frame %d: %d verts", parent.GetFrameNumber(), access.getVertexCount()));
        final int newVertexCount = access.getVertexCount();
//        CVKAssert(vertexCount != newVertexCount); //REMOVE AFTER TESTING
        
        // If we have had an update task called before a rebuild task we first have to build
        // the staging buffer.  Rebuild also if we our vertex count has somehow changed.
        final boolean rebuildRequired = cvkVertexFlagsStagingBuffer == null || vertexCount != access.getVertexCount();
        if (rebuildRequired) {
            RebuildVertexFlagsStagingBuffers(access, newVertexCount);
        } else {
            int minMax[] = change.getRange();
            CVKAssert(minMax != null);
            final int numChanges = change.getSize();
            try {
                vertexFlagsStagingBufferLock.lock();
                
                final int mappingStart = minMax[0] * FLAGS_STRIDE;
                final int mappingSize  = ((minMax[1] - minMax[0]) + 1) * FLAGS_STRIDE;
                ByteBuffer pVertexFlagsMemory = cvkVertexFlagsStagingBuffer.StartWrite(mappingStart, mappingSize);
                for (int i = 0; i < numChanges; ++i) {
                    int pos = change.getElement(i);
                    SetVertexFlags(pos, minMax[0], pVertexFlagsMemory, access);                               
                }
                cvkVertexFlagsStagingBuffer.EndWrite();
                pVertexFlagsMemory = null; // now unmapped, do not use
            } finally {
                vertexFlagsStagingBufferLock.unlock();
            }   
        }
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (rebuildRequired) {
                SetVertexFlagsBufferState(CVK_RESOURCE_NEEDS_REBUILD);
            } else if (vertexFlagsBufferState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexFlagsBufferState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };         
    }
    
    public CVKRenderableUpdateTask TaskUpdateColours(final VisualChange change, final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        CVKLOGGER.info(String.format("TaskUpdateColours frame %d: %d verts", parent.GetFrameNumber(), access.getVertexCount()));
        
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            if (vertexBuffersState != CVK_RESOURCE_NEEDS_REBUILD) {
                SetVertexBuffersState(CVK_RESOURCE_NEEDS_UPDATE);
            }
        };             
    }   
    
    public CVKRenderableUpdateTask TaskSetHighLightColour(final VisualAccess access) {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
        CVKLOGGER.info(String.format("TaskSetHighLightColour frame %d: %d verts", parent.GetFrameNumber(), access.getVertexCount()));
        final ConstellationColor highlightColor = access.getHighlightColor();
                                                
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {
            geometryUBO.highlightColor.set(0, 0, highlightColor.getRed());
            geometryUBO.highlightColor.set(1, 1, highlightColor.getGreen());
            geometryUBO.highlightColor.set(2, 2, highlightColor.getBlue());
          
            SetGeometryUBOState(CVK_RESOURCE_NEEDS_UPDATE);
        };             
    }
    
    public CVKRenderableUpdateTask TaskUpdateCamera() {
        //=== EXECUTED BY CALLING THREAD (VisualProcessor) ===//
                                                
        //=== EXECUTED BY RENDER THREAD (during CVKVisualProcessor.ProcessRenderTasks) ===//
        return () -> {          
            SetVertexUBOState(CVK_RESOURCE_NEEDS_UPDATE);
        };           
    }
    
    
    // ========================> Helpers <======================== \\      
    
    private Vector4i MakeIconIndexes(final int pos, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(pos < access.getVertexCount());
        
        final String foregroundIconName = access.getForegroundIcon(pos);
        final String backgroundIconName = access.getBackgroundIcon(pos);
        final int foregroundIconIndex = parent.GetTextureAtlas().AddIcon(foregroundIconName);
        final int backgroundIconIndex = parent.GetTextureAtlas().AddIcon(backgroundIconName);

        final String nWDecoratorName = access.getNWDecorator(pos);
        final String sWDecoratorName = access.getSWDecorator(pos);
        final String sEDecoratorName = access.getSEDecorator(pos);
        final String nEDecoratorName = access.getNEDecorator(pos);
        final int nWDecoratorIndex = nWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sWDecoratorIndex = sWDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sWDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int sEDecoratorIndex = sEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(sEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;
        final int nEDecoratorIndex = nEDecoratorName != null ? parent.GetTextureAtlas().AddIcon(nEDecoratorName) : CVKIconTextureAtlas.TRANSPARENT_ICON_INDEX;

        final int icons = (backgroundIconIndex << ICON_BITS) | (foregroundIconIndex & ICON_MASK);
        final int decoratorsWest = (sWDecoratorIndex << ICON_BITS) | (nWDecoratorIndex & ICON_MASK);
        final int decoratorsEast = (nEDecoratorIndex << ICON_BITS) | (sEDecoratorIndex & ICON_MASK);

        return new Vector4i(icons, decoratorsWest, decoratorsEast, access.getVertexId(pos));
    }      
    
    private void SetIconIndexes(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(vertex != null);        
        vertex.data.set(MakeIconIndexes(pos, access));
    }    
    
    private void SetColorInfo(final int pos, CVKIconsRenderable.Vertex vertex, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(vertex != null);
        CVKAssert(pos < access.getVertexCount());
        
        vertex.SetBackgroundIconColour(access.getVertexColor(pos));
        vertex.SetVertexVisibility(access.getVertexVisibility(pos));
    }    
    
    // TODO_TT: find out more about the second coord
    // TODO_TT: see if anything ever uses the radius   - yes the blaze batcher 
    private void SetVertexPosition(final int pos, ByteBuffer buffer, final VisualAccess access) {
        CVKAssert(access != null);
        CVKAssert(buffer.remaining() >= POSITION_STRIDE);    
        
        buffer.putFloat(access.getX(pos));
        buffer.putFloat(access.getY(pos));
        buffer.putFloat(access.getZ(pos));
        buffer.putFloat(access.getRadius(pos));
        buffer.putFloat(access.getX2(pos));
        buffer.putFloat(access.getY2(pos));
        buffer.putFloat(access.getZ2(pos));
        buffer.putFloat(access.getRadius(pos));                 
    }  
    
    private void SetVertexFlags(final int vertexIndex, final int bufferStartIndex, ByteBuffer buffer, final VisualAccess access) {
        final boolean isSelected = access.getVertexSelected(vertexIndex);
        final boolean isDimmed = access.getVertexDimmed(vertexIndex);
        byte flags = (byte)((isDimmed ? DIMMED_BIT : 0) | (isSelected ? SELECTED_BIT : 0));
        
        // buffer may be partially only mapped so we need to offset the write location
        int writeLocation = vertexIndex - (bufferStartIndex * FLAGS_STRIDE);
        CVKAssert(writeLocation >= 0);
        CVKAssert(writeLocation < buffer.capacity());
        
        buffer.put(writeLocation, flags);
    }    
}
