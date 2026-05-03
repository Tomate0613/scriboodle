#version 330

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

in vec3 Position;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in float Radius;
in vec4 FillColor;

out vec2 UV;
out float radius;
out vec4 fillColor;
flat out ivec2 mouse;
flat out ivec2 dimensions;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    radius = Radius;
    UV = UV0;
    dimensions = UV1;
    mouse = UV2;
    fillColor = FillColor;
}
