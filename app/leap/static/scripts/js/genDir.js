
/**
 * Generate resource list
 * @param {*} resJson 
 */
function genDir(resJson) {            
    var data = JSON.parse(resJson);    
    var host = data.host.substring(0, data.host.lastIndexOf('/'));
    var path = data.path.substring(1, data.path.length);
    var parent = data.parent;
    var elements = data.elements;
    var accum = '';
    var sublink = path == '' ? '' : "</a>"+path.split('/').map(e => {
        var l = '<a class=\"font-large\" href=\"'+accum+"/"+e+'\">/'+e+'</a>';
        accum += "/" + e;
        return l;
        }).join('');
    var link = '<a href=\"/directory?reqPath=%2F\"><img class=\"img\" src=\"/img/logo32.png\"/></a>  <b class=\"font-large\">Leap Directory - </b><a class=\"font-large\" href=\"/directory?reqPath=%2F\">'+host+'</a>' + sublink;
    console.log(link);
    document.getElementById("host").insertAdjacentHTML("afterbegin", link);
    //document.getElementById("path").insertAdjacentText("afterbegin", data.path);
    document.getElementById("parent").setAttribute('href', data.parent);
    
    var dir = document.getElementById("resource-list");
    var resList = "";
    for(let i = 0; i < elements.length; i++) {
        resList += '<span class=\"file-name\" style="width:30%;display:inline-block;"><img class=\"img\" src=\"'+elements[i].img+'\"><a href=\"'+elements[i].uri+'\">'+elements[i].file+'</a></span>'
              +'<span class=\"last-modified\" style="width:30%;display:inline-block;">'+elements[i].lastModified+'</span>'
              +'<span class=\"file-size\" style="width:20%;display:inline-block;">'+elements[i].size+'</span>'
              +'<span class=\"desc\" style="width:20%;display:inline-block;">'+elements[i].desc+'</span>'
              +'<br>';
    }
    dir.insertAdjacentHTML("afterbegin", resList);
}
