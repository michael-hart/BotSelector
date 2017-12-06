#version 330 core

layout(location = 0) in vec4 position;
// No location for texture

// Pipe the colour to the fragment shader
uniform vec4 colour = vec4(1, 0, 0, 1);
out vec4 rgba;

// Projection matrix is the orthographic projection of the camera
uniform mat4 pr_matrix = mat4(1, 0, 0, 0,
                              0, 1, 0, 0,
                              0, 0, 1, 0,
                              0, 0, 0, 1);
// View matrix is the camera position
uniform mat4 vw_matrix = mat4(1, 0, 0, 0,
                              0, 1, 0, 0,
                              0, 0, 1, 0,
                              0, 0, 0, 1);
// Transform matrices are used for positions
uniform mat4 translate = mat4(1, 0, 0, 0,
                              0, 1, 0, 0,
                              0, 0, 1, 0,
                              0, 0, 0, 1);
uniform mat4 scale = mat4(1, 0, 0, 0,
                          0, 1, 0, 0,
                          0, 0, 1, 0,
                          0, 0, 0, 1);


void main() {
    gl_Position = pr_matrix * vw_matrix * translate * scale * position;
    rgba = colour;
}
