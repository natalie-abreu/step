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


// js for templated slideshows
let slideIndex = 0;
let show_title = 'b';
let imgs = []
for (i = 1; i <=5; i++) {
        imgs.push("/images/"+show_title+i+".jpg")
}

function renderSlides(show) {
    imgs = [];
    show_title = show;
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
    show_title = options[showIndex][0].toLowerCase();
    const slidesTitle = document.getElementById('slides-title');
    slidesTitle.innerHTML = options[showIndex];
    renderSlides(show_title, 0);
}

function toggleProjectOn(id) {
    descriptions = [`Through a student-org I am in called Code the Change, I worked on a web dev project featuring an interactive journal 
            to aid students with college application process as part of the PLUS ME Project, using React, Express, Node.js, and Postgres.`,
            `Through another student-org I am in, CAIS++, I worked on a group project to create a generalized classifier for genomic data.`,
            `This was my project for Google CSSI last summer that used the Google Maps API to find restaurants in the user's area based on user food preferences.`,
            `I worked in a research lab at USC where I studied ML and RL techniques with the goal of modeling human-inspired multirobot coordination.`]
    
    const ind = id.split("-")[1]-1;
    const img = id.split("-")[0]+"-img";

    // don't have an image for research square
    if (img != "research-img") {
        const background = document.getElementById(img);
        background.style.display = "none";
    }

    const square = document.getElementById(id);
    square.innerHTML = descriptions[ind];
    square.style.fontSize = "18px";
    square.style.lineHeight = "18px";
    square.style.paddingTop = "80px";
    square.style.height = "220px";
    square.style.background = "rgb(64, 78, 77, .6)";

}

function toggleProjectOff(id) {
    titles = ["PlusMe Project", "gsec", "Todo Restaurants", "Research Lab"];

    const ind = id.split("-")[1]-1;
    const img = id.split("-")[0]+"-img";
    
    // don't have an image for research square
    if (img != "research-img") {
        const background = document.getElementById(img);
        background.style.display = "block";
    }
    
    const square = document.getElementById(id);
    square.innerHTML=titles[ind];
    square.style.fontSize = "30px";
    square.style.lineHeight = "300px";
    square.style.paddingTop = "0px";
    square.style.height = "300px";
    square.style.background = "rgb(64, 78, 77, .25)";
}

let page_num = 1;
async function getComments(pageInc=0, numComments=0) {
    numComments = await restoreNumComments(numComments);
    page_num+=pageInc;
    if (page_num == 0) page_num=1;
    const response = await fetch(`/data?max=${numComments}&page=${page_num}`);
    // will catch case when page is out of bounds
    let comments;
    try {
        comments = await response.json();
    } catch(e) {
        page_num-=1;
        console.log(e);
        return;
    }
    let board = document.getElementById("comments-board");
    board.innerText = '';
    for (msg of comments) {
        board.appendChild(createComment(msg));
    }
}

function createComment(msg) {
  const comment = document.createElement('div');
  comment.className = "comment";

  const initial = document.createElement('div');
  initial.className = "comment-initial";
  if (msg.name != "") initial.innerText = msg.name[0].toUpperCase();
  initial.id = msg.id + "-initial";

  const popup = document.createElement('span');
  popup.className = "comment-popup";
  popup.innerText = msg.name;
  popup.id = msg.id + "-popup";
  initial.appendChild(popup);
  initial.onmouseover = function() { showCommentInfo(initial.id); };
  initial.onmouseout = function() { hideCommentInfo(initial.id); };

  const message = document.createElement('div');
  message.className = "comment-message";
  message.innerText = msg.message;

  comment.appendChild(initial);
  comment.appendChild(message);

  return comment;
}

async function clearComments() {
    page_num = 0;
    const request = new Request('/delete-data', {method: 'POST'});
    await fetch(request);
    getComments();
}

function restoreNumComments(numComments) {
    // prevent resetting of dropdown selection on refresh/submit
    // use default 0 to indicate that user has not selected a # of comments
    if (numComments == 0) {
        if (!sessionStorage.numComments) {
            // if nothing in session storage, show 5 comments
            numComments = 5;
            sessionStorage.numComments = numComments;
        }
        else numComments = sessionStorage.numComments;
    }
    else {
        sessionStorage.numComments = numComments;
    }
    let maxSelection = document.getElementById("max-selection");
    maxSelection.value = numComments;
    return numComments;
}

function showCommentInfo(id) {
    id = id.split("-")[0]+"-popup";
    const popup = document.getElementById(id);
    popup.style.display = "block";
}

function hideCommentInfo(id) {
    id = id.split("-")[0]+"-popup";
    const popup = document.getElementById(id);
    popup.style.display = "none";
}