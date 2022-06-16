
const interval = '<!--@interval-->';

//function refreshImage(interval) {
  setInterval( function() {
    $("#cpu").attr("src", "monitor/cpu.png?"+new Date().getTime());
    $("#memory").attr("src", "monitor/memory.png?"+new Date().getTime());
    $("#thread").attr("src", "monitor/thread.png?"+new Date().getTime());
    console.log("send request");
    }, interval);        


//}

//window.onload = () => refreshImage(interval);
