precision mediump float;

uniform mat4 modelViewMatrix;
uniform mat4 modelViewProjectionMatrix;

attribute vec4 position;
attribute vec3 normal;

varying vec3 n;
varying vec3 vertex_to_light_vector[3];
varying vec3 vertex_to_camera_vector;

uniform vec3 cameraPos;

uniform vec4 additionalColor;
uniform vec4 ambientColor;

varying vec4 vertexColor;

void main()
{
    vertexColor = ambientColor + additionalColor;

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

    vertex_to_light_vector[0] = normalize(vec3(vec3(-100.0, 100.0, 50.0) - vertex_in_modelview_space.xyz));
    vertex_to_light_vector[1] = normalize(vec3(vec3(-100.0, -100.0, -50.0) - vertex_in_modelview_space.xyz));
    vertex_to_light_vector[2] = normalize(vec3(vec3(100.0, 0.0, -200.0) - vertex_in_modelview_space.xyz));

    vertex_to_camera_vector = normalize(vec3(cameraPos - vertex_in_modelview_space.xyz));
}