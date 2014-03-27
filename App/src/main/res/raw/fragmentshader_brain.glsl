
precision mediump float;

varying vec3 n;
varying vec3 vertex_to_light_vector;

uniform int transparent;

void main()
{
    const vec4 AmbientColor = vec4(0.15, 0.15, 0.15, 1.0);
    const vec4 DiffuseColor = vec4(0.4, 0.4, 0.4, 1.0);
    // Defining The Material Colors


    // Scaling The Input Vector To Length 1
    vec3 normalized_normal = normalize(n);
    vec3 normalized_vertex_to_light_vector = normalize(vertex_to_light_vector);

    normalized_vertex_to_light_vector.y = 0.0;
    normalized_vertex_to_light_vector.z = 0.0;

    // Calculating The Diffuse Term And Clamping It To [0;1]
    float DiffuseTerm = clamp(dot(normalized_normal, normalized_vertex_to_light_vector), 0.0, 1.0);


    vec3 col = (AmbientColor*0.8 + DiffuseColor*0.8 * DiffuseTerm).xyz;

    float blend = length(col);

    // Calculating The Final Color
    gl_FragColor = vec4(col, blend);
}