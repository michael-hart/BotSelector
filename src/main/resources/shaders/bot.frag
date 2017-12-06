#version 330 core

in vec4 rgba;
layout (location = 0) out vec4 colour;

void main(){
    colour = rgba;
}
