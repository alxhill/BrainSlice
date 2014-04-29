precision mediump float;

uniform mat4 modelViewMatrix;
uniform mat4 modelViewProjectionMatrix;

attribute vec4 position;

void main()
{
    gl_Position = modelViewProjectionMatrix * position;
}