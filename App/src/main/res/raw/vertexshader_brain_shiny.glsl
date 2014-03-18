precision mediump float;

uniform mat4 modelViewMatrix;
uniform mat4 modelViewProjectionMatrix;

attribute vec4 position;
attribute vec3 normal;


varying vec3 n;
varying vec3 vertex_to_light_vector;

uniform vec3 lightPositions[8];

uniform vec3 cameraPos;
 
void main()
{
    // Transforming The Vertex
    gl_Position = modelViewProjectionMatrix * position;
 
    // Transforming The Normal To ModelView-Space
    n = normal;
 
    // Transforming The Vertex Position To ModelView-Space
    vec4 vertex_in_modelview_space = modelViewMatrix * position;
 
    // Calculating The Vector From The Vertex Position To The Light Position
    vertex_to_light_vector = normalize(vec3(lightPositions[0] - vertex_in_modelview_space.xyz));
    //vertex_to_light_vector = normalize(vec3(vec3(-100, 0, 0) - position.xyz));
}