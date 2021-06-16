
Screen Space Ambient Occlusion with DirectX Compute Shader

By: Jason Zink

This sample program demonstrates an implementation of the screen space ambient occlusion algorithm in the DirectX 11 Compute Shader.  It is built on top of the DXUT 11 framework provided with the August 2009 DXSDK, and also provides some utility classes for loading Milkshape3D files and building the vertex and index buffers to render them.

At startup, the program will request a hardware D3D11 feature level.  If it is not available, then it will fall back to the reference rasterizer.  

*IMPORTANT NOTE* - the reference rasterizer requires the August 2009 DXSDK or later to be installed, so please ensure that this is the case!

On first generation DX11 hardware the framerate ranges in the hundreds, while on the reference rasterizer it can take up to a couple of minutes to generate a frame.  If using the reference rasterizer, please be patient and you will see the images rendered very slowly.

Usage Notes:
There is a single compile flag available for the C++ code - 'USE_16x16'.  When defined, the program will use the 16x16 thread groups.  If it is not defined, then the program will use 32x32 thread groups when processing the ambient occlusion buffer.

In the ambient occlusion shader files, additional compile flags have been used to specify if the group shared memory will be used as a depth sample cache.  If 'USE_GSM' is defined, then that shader will use the GSM, otherwise it will directly read from the depth texture for all samples.  In addition, if the GSM is being used then a second define is available - 'USE_GATHER' which will retrieve 4 samples at a time with the Gather instruction.  Otherwise each sample will be individually loaded one at a time.

In any of the cases where the GSM is used, if the screen space offset extends beyond the cached depths in that thread group, then artifacts will be seen.  As discussed in the article, this can be overcome by modifying the offset based on the distance to the viewer, or that sample can be dynamically tossed out if it falls outside the valid range.