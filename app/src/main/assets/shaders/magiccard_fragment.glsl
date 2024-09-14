#version 310 es
precision mediump float;

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoords;

out vec4 FragColor;

uniform vec4 uColor;

void main() {
    FragColor = uColor;
}