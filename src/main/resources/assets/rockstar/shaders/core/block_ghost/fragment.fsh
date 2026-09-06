#version 150

#moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 texel = texture(Sampler0, texCoord0);

    // отсечку считаем по текстуре, а не по итоговому цвету: альфа у призрака своя и как раз уезжает
    // к нулю у камеры — на общем пороге 0.1 он бы не таял, а гас скачком
    if (texel.a < 0.1) {
        discard;
    }

    vec4 color = texel * vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
