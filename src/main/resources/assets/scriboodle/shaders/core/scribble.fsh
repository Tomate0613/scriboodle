#version 330

// Can't moj_import in things used during startup, when resource packs don't exist.
// This is a copy of dynamicimports.glsl
layout (std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

uniform sampler2D Sampler0;

in vec2 UV;
in vec4 fillColor;
in float radius;
flat in ivec2 mouse;
flat in ivec2 dimensions;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, UV);

    vec2 cursorOffset = vec2(mouse) - (UV * vec2(dimensions));
    float dist = length(cursorOffset);

    if (dist > radius - fwidth(dist) && dist < radius) {
        if (color.a < .5) {
            color = vec4(1, 1, 1, 1);
        }

        color = vec4(1 - color.rgb, 1);
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
