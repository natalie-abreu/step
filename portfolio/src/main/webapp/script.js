// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

function switchPage(id) {
    // const activeTab = document.getElementById(id);
    const allContent = document.getElementsByClassName("contentdiv");
    for (item of allContent) {
        item.style.display = "none";
        console.log(item.id)   
     }
    const res = id.split("-");
    console.log(res[0]+"-div")
    const activeText = document.getElementById(res[0]+"-div");
    activeText.style.display = "block";
}
let slideIndex = 0;
let show_name;
let imgs = []
function renderSlides(show) {
    imgs = [];
    console.log(show);
    show_name = show;
    for (i = 1; i <=5; i++) {
        imgs.push("/images/"+show+i+".jpg")
    }
    let w = '300px';
    let h = '400px';
    if (show == 'p') {
        [w, h] = [h, w];
    }
    const slideshowArea = document.getElementById("slideshow-container-container");
    slideshowArea.style.display = "flex";
    // slideshowArea.style.width = w;
    // slideshowArea.style.height = h;
    const container = document.getElementById('slideshow-container');
    container.style.display = "block";
    container.style.width = w;
    container.style.height = h;
    container.innerHTML = `<div class='slides' style='display:block'> 
    <img id='slide-img' src=${imgs[0]} style='width:${w}; height:${h}; object-fit: cover;'>`
}

function changeSlides(dir) {
    console.log("SHOW" + show_name)
    if (dir == 0) {
        slideIndex = 0;
    }
    else if (slideIndex == 0 && dir == -1) {
        slideIndex = imgs.length-1;
    }
    else if (slideIndex == imgs.length-1 && dir == 1) {
        slideIndex = 0;
    }
    else {
        slideIndex+=dir;
    }
    const slideImage = document.getElementById('slide-img');
    slideImage.src = imgs[slideIndex];

}