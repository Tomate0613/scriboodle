#version 330
#extension GL_ARB_separate_shader_objects : require

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    mat4 TextureMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
};

uniform sampler2D Sampler0;

layout (location = 0) in vec2 UV;
layout (location = 1) flat in float radius;
layout (location = 2) in vec4 fillColor;
layout (location = 3) flat in ivec2 mouse;
layout (location = 4) flat in ivec2 dimensions;

layout (location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, UV);

    vec2 cursorOffset = vec2(mouse) - (UV * vec2(dimensions)) + vec2(.5, .5);
    float dist = length(cursorOffset);

    if (dist > radius - fwidth(dist) && dist < radius) {
        vec4 prev = color;

        if (color.a < .5) {
            color = vec4(1, 1, 1, 1);
        }

        color = vec4(1 - color.rgb, 1);

        float diff = length(color - prev);

        if (diff < 0.3)
        {
            color = vec4(1, 1, 1, 1);
        }

    } else if (dist < radius && fillColor.a > 0 && (fillColor.a * 2 - 1) * radius > cursorOffset.y) {
        //    } else if(dist < (radius * fillColor.a)) {
        //        color = fillColor * fillColor.a + color * (1 - fillColor.a);
        color = fillColor;
    }

    if (color.a == 0.0) {
        discard;
    }

    fragColor = color * ColorModulator;
}
