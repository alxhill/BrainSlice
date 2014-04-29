precision mediump float;

varying vec3 n;
varying vec3 vertex_to_light_vector[3];
varying vec3 vertex_to_camera_vector;

uniform int transparent;

uniform vec3 cameraPos;

void main()
{
    // Defining The Material Colors
    const vec4 AmbientColor = vec4(0.48, 0.35, 0.35, 1.0);
    const vec4 DiffuseColor = vec4(0.62, 0.4, 0.4, 1.0);
    const vec4 specularColor = vec4(0.2, 0.2, 0.2, 1.0);

    const float aS = 0.5;
    const float dS = 2.7;
    const float kS = 3.5;

    // Scaling The Input Vector To Length 1
    vec3 normalized_normal = normalize(n);

    vec3 final_col = vec3(0.0,0.0,0.0);

    for(int i=0; i<3; i++)
    {
        //vec3 new_vec = vec3(-vertex_to_light_vector[i].x, vertex_to_light_vector[i].yz);

        vec3 normalized_vertex_to_light_vector = normalize(vertex_to_light_vector[i]);

        vec3 H = normalize(vertex_to_light_vector[i] + vertex_to_camera_vector);

        float p = (dot(n, H));
        float npow = 10.0;

        // Calculating The Diffuse Term And Clamping It To [0;1]
        float DiffuseTerm = clamp(dot(normalized_normal, normalized_vertex_to_light_vector), 0.0, 1.0);

        float specularTerm = clamp(kS*pow(p, npow), 0.0, 1.0);

        vec3 col = (dS*DiffuseColor * DiffuseTerm).xyz + specularColor.xyz*specularTerm;
        final_col = final_col + col;
    }

    final_col = final_col / 3.0;

    // Calculating The Final Color
    gl_FragColor = vec4(final_col + aS*AmbientColor.xyz, 1.0);// + aS*AmbientColor;
}