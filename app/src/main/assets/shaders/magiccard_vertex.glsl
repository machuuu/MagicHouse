#version 310 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

out vec3 FragPos;
out vec3 Normal;
out vec2 TexCoords;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform mat2 uTextureScale;

void main() {

    gl_Position = uProjection * uView * uModel * vec4(aPos, 1.0);

    // Calculate the normal to the shape in world space
    //Normal = vec3(inverse(transpose(uModel)) * vec4(aNormal, 1.0));
    Normal = mat3(transpose(inverse(uModel))) * aNormal;

    // Calculate World Space Coordinate of Fragment
    FragPos = vec3(uModel * vec4(aPos, 1.0));

    // Compute TexCoords to Clip Space
    TexCoords = ((aTexCoord - vec2(0.5,0.5))) + vec2(0.5,0.5);
    //TexCoords = (uTextureScale * (aTexCoord - vec2(0.5,0.5))) + vec2(0.5,0.5);
}

