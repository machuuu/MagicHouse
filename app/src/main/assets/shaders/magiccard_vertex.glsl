#version 310 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out vec3 FragPos;
out vec3 Normal;
out vec2 TexCoords;

uniform mat4 uMVPMatrix;

void main() {
    gl_Position = uMVPMatrix * vec4(aPos, 1.0);
    FragPos = vec3(1.0,1.0,1.0);
    Normal = vec3(1.0,1.0,1.0);
    TexCoords = vec2(1.0,1.0);
}