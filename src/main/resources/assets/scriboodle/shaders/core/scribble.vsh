#version 330
#extension GL_ARB_separate_shader_objects : require

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl and projection.glsl
layout (std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout (std140) uniform Projection {
    mat4 ProjMat;
};

layout (location = 0) in vec3 Position;
layout (location = 1) in vec4 Color;
layout (location = 2) in vec2 UV0;
layout (location = 3) in ivec2 UV1;
layout (location = 4) in ivec2 UV2;
layout (location = 5) in float LineWidth;

layout (location = 0) out vec2 UV;
layout (location = 1) flat out float radius;
layout (location = 2) out vec4 fillColor;
layout (location = 3) flat out ivec2 mouse;
layout (location = 4) flat out ivec2 dimensions;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    radius = LineWidth;
    UV = UV0;
    dimensions = UV1;
    mouse = UV2;
    fillColor = Color;
}
