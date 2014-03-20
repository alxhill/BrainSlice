precision mediump float;

varying vec3 n;
varying vec3 vertex_to_light_vector[8];
varying vec3 vertex_to_camera_vector;

uniform int transparent;

uniform vec3 cameraPos;

uniform vec3 lightPositions[8];

void main()
{
    // Defining The Material Colors
    const vec4 AmbientColor = vec4(0.48, 0.35, 0.35, 1.0);
    const vec4 DiffuseColor = vec4(0.62, 0.4, 0.4, 1.0);
    const vec4 specularColor = vec4(0.2, 0.2, 0.2, 1.0);

    const float aS = 0.5;
    const float dS = 0.7;
    const float kS = 3.5;

    // Scaling The Input Vector To Length 1
    vec3 normalized_normal = normalize(n);

    vec3 final_col = vec3(0.0,0.0,0.0);

    for(int i=0; i<2; i++)
    {
        vec3 normalized_vertex_to_light_vector = normalize(vertex_to_light_vector[i]);

        vec3 H = normalize(vertex_to_light_vector[i] + vertex_to_camera_vector);

        float p = (dot(n, H));
        float npow = 10.0;

        // Calculating The Diffuse Term And Clamping It To [0;1]
        float DiffuseTerm = clamp(dot(normalized_normal, normalized_vertex_to_light_vector), 0.0, 1.0);

        //vec3

        float specularTerm = clamp(kS*pow(p, npow), 0.0, 1.0);

        vec3 col = (dS*DiffuseColor * DiffuseTerm).xyz + specularColor.xyz*specularTerm;
        final_col = final_col + col;
    }

    final_col = final_col / 2.0;

    // Calculating The Final Color
    gl_FragColor = vec4(final_col + aS*AmbientColor.xyz, 1.0);// + aS*AmbientColor;

    //gl_FragColor = vec4(0.5, 0.5, 0.5, 0.5);
}





/*precision mediump float;

 uniform int transparent;

 varying vec4 lightVec[2];
 varying vec4 eyeVec;

 void main ()
 {
 	float att = 1.0;

 	//float distSqr = dot(lightVec[0], lightVec[0]);
 	//float att = clamp(sqrt(distSqr), 0.0, 1.0);
 	//vec3 lVec = lightVec[0] * inversesqrt(distSqr);

 	//vec4 vDiffuse = vec4(1.0. 1.0, 1.0, 1.0);

 	//float specular = pow(clamp(dot(-lVec, vVec), 0.0, 1.0), 0.85);
 	//vec4 vSpecular = vec4(specularColors[0],0) * specular;

 	//gl_FragColor = (vAmbient*base + vDiffuse*base + vSpecular) * att*2.0;

 	gl_FragColor = gl_Normal;
 }*/