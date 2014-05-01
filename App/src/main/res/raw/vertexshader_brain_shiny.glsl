precision mediump float;

uniform mat4 modelViewMatrix;
uniform mat4 modelViewProjectionMatrix;

attribute vec4 position;
attribute vec3 normal;

varying vec3 n;
varying vec3 vertex_to_light_vector[6];
varying vec3 vertex_to_camera_vector;

uniform vec3 cameraPos;

uniform vec4 additionalColor;
uniform vec4 ambientColor;

varying vec4 vertexColor;

varying vec2 texCoord;
uniform mat4 textureMatrix;

attribute vec2 texture0;

void main()
{
    vertexColor = ambientColor + additionalColor;

    texCoord = (textureMatrix * vec4(texture0, 0.0, 1.0)).xy;


    // Transforming The Vertex
    gl_Position = modelViewProjectionMatrix * position;
 
    // Transforming The Normal To ModelView-Space
    n = normal;
 
    // Transforming The Vertex Position To ModelView-Space
    vec4 vertex_in_modelview_space = modelViewMatrix * position;

    //(100, 100, 0)
    //(100, -100, 0
 
    // Calculating The Vector From The Vertex Position To The Light Position
    /*vertex_to_light_vector[0] = normalize(vec3(lightPositions[0] - vertex_in_modelview_space.xyz));
    vertex_to_light_vector[1] = normalize(vec3(lightPositions[1] - vertex_in_modelview_space.xyz));
    vertex_to_light_vector[2] = normalize(vec3(lightPositions[2] - vertex_in_modelview_space.xyz));*/

    vertex_to_light_vector[0] = normalize(vec3(vec3(100.0, 100.0, 50.0) - vertex_in_modelview_space.xyz));
    vertex_to_light_vector[1] = normalize(vec3(vec3(100.0, -100.0, -50.0) - vertex_in_modelview_space.xyz));
    vertex_to_light_vector[2] = normalize(vec3(vec3(-100.0, 0.0, -200.0) - vertex_in_modelview_space.xyz));
    /*for(int i=3; i<6; i++)
    {
        vertex_to_light_vector[i] = -vertex_to_light_vector[i-3];
    }*/

    vertex_to_camera_vector = normalize(vec3(cameraPos - vertex_in_modelview_space.xyz));

    //gl_TexCoord[0] = gl_MultiTexCoord0;
}