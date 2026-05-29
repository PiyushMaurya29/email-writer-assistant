console.log("Email Writer Assistant Loaded");

const API_URL = "http://localhost:8081/api/email/generate";

function createButton() {

    const button = document.createElement("button");

    button.innerText = "AI Reply";

    button.style.marginRight = "10px";
    button.style.padding = "10px";
    button.style.backgroundColor = "#0b57d0";
    button.style.color = "white";
    button.style.border = "none";
    button.style.borderRadius = "5px";
    button.style.cursor = "pointer";

    return button;
}

function getToolbar() {

    return document.querySelector(".btC");
}

function getEmailContent() {

    const emailElement = document.querySelector(".adn .a3s, .a3s");

    return emailElement ? emailElement.innerText : "";
}

function getReplyBox() {

    return document.querySelector('[role="textbox"][contenteditable="true"]');
}

async function generateReply(emailContent) {

    try {

        const response = await fetch(API_URL, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify({
                emailContent: emailContent,
                tone: "professional"
            })
        });

        console.log("Response Status:", response.status);

        const data = await response.text();

        console.log("Response Data:", data);

        if (!response.ok) {
            throw new Error(data || `Backend returned ${response.status}`);
        }

        return data;

    } catch (error) {

        console.error("FETCH ERROR:", error);

        return "Unable to connect to backend.";
    }
}

async function handleGenerateReply(button) {

    button.innerText = "Generating...";
    button.disabled = true;

    const emailContent = getEmailContent();

    if (!emailContent.trim()) {
        button.innerText = "AI Reply";
        button.disabled = false;
        alert("Open an email before generating a reply.");
        return;
    }

    const generatedReply = await generateReply(emailContent);

    if (isErrorMessage(generatedReply)) {
        alert(generatedReply);
        button.innerText = "AI Reply";
        button.disabled = false;
        return;
    }

    const replyBox = getReplyBox();

    if (replyBox) {

        replyBox.focus();

        document.execCommand("insertText", false, generatedReply);

        replyBox.dispatchEvent(new InputEvent("input", {
            bubbles: true,
            inputType: "insertText",
            data: generatedReply
        }));
    }

    button.innerText = "AI Reply";
    button.disabled = false;
}

function isErrorMessage(message) {

    return message.startsWith("Error generating AI reply")
        || message.includes("API key is missing")
        || message.startsWith("Unable to connect")
        || message.startsWith("Failed to parse")
        || message.startsWith("No AI response");
}

function injectButton() {

    const toolbar = getToolbar();

    if (!toolbar) return;

    if (document.querySelector(".ai-reply-button")) return;

    const button = createButton();

    button.classList.add("ai-reply-button");

    button.addEventListener("click", () => {

        handleGenerateReply(button);
    });

    toolbar.prepend(button);
}

const observer = new MutationObserver(() => {

    injectButton();
});

observer.observe(document.body, {

    childList: true,
    subtree: true
});
