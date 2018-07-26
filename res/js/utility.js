function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) {
            return unescape(sParameterName[1]);
        }
    }
}

function loadScript(src, onload) {
    var head = document.getElementsByTagName("head")[0];
    var script = document.createElement("script");
    script.type = "text/javascript";
    script.src = src;
    script.onload = onload;
    script.onreadystatechange = function() {
        if (this.readyState == 'complete' || this.readyState == 'loaded') {
            onload();
        }
    };
    head.appendChild(script);
}

var oldOnload = window.onload || function () {};
window.onload = function initializeMoreBranch() {
    oldOnload();
    var moreBranch = document.getElementById("more-branch");
    if (moreBranch) {
        var url = window.location.pathname + window.location.search;
        var branches = moreBranch.innerHTML.split(':');
        var thisBranch = branches[0];
        var otherBranches = branches[1].split('|');
        var targetHtml = "";
        for (var i = otherBranches.length - 1; i >= 0; i--) {
            var targetBranch = otherBranches[i];
            if (thisBranch != targetBranch) {
                var targetUrl = url.replace(thisBranch, targetBranch);
                targetHtml += "<a href='" + targetUrl + "' class='external'>" + targetBranch + "</a><br/>";
            }
        }
        moreBranch.innerHTML = targetHtml;
    }
}
