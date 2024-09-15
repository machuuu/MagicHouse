#version 310 es
precision mediump float;

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoords;

out vec4 FragColor;

struct Light {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

struct Material {
    vec3 frontcolor;
    vec3 backcolor;
    float shininess;
};

uniform Material object;
uniform Light light;
uniform vec3 viewPos; // not set, initialize to 0.0 assuming camera is fixed in world

void main() {
    // ambient
    vec3 ambient = light.ambient;

    // diffuse
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(light.position - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * light.diffuse;

    // specular
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), object.shininess);
    vec3 specular = light.specular * spec;

    // sum it up
    vec3 result = (specular * ambient + diffuse) * object.frontcolor;
    FragColor = vec4(result, 1.0);
}
