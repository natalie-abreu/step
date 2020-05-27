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
    const allContent = document.getElementsByClassName("contentdiv");
    for (item of allContent) {
        item.style.display = "none";
     }
    const res = id.split("-");
    const activeText = document.getElementById(res[0]+"-div");
    activeText.style.display = "block";
}
let slideIndex = 0;
let show_name = 'b';
let imgs = []
for (i = 1; i <=5; i++) {
        imgs.push("/images/"+show_name+i+".jpg")
}
function renderSlides(show) {
    imgs = [];
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
    const container = document.getElementById('slideshow-container');
    container.style.display = "block";
    container.style.width = w;
    container.style.height = h;
    container.innerHTML = `<div class='slides' style='display:block'> 
    <img id='slide-img' src=${imgs[0]} style='width:${w}; height:${h}; object-fit: cover;'>`
}

function changeSlides(dir) {
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

const options =["Baking", "Places", "Me and Other People"];
const show_ids = ["b", "p", "m"]
console.log("testing: " + options[0][0].toLowerCase());
let showIndex = 0;
function changeShow(dir) {
    if (dir == 0) {
        showIndex = 0;
    }
    else if (showIndex == 0 && dir == -1) {
        showIndex = options.length-1;
    }
    else if (showIndex == options.length-1 && dir == 1) {
        showIndex = 0;
    }
    else {
        showIndex+=dir;
    }
    show_name = options[showIndex][0].toLowerCase();
    const slidesTitle = document.getElementById('slides-title');
    slidesTitle.innerHTML = options[showIndex];
    renderSlides(show_name, 0);
}