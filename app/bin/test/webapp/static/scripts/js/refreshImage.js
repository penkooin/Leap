
const interval = '<!--@interval-->';
const url = '<!--@url-->';

function refreshImage(interval) {
  setInterval( function() {
    $("#cpu").attr("src", url+"/monitor/cpu.png?"+new Date().getTime());
    $("#thread").attr("src", url+"/monitor/thread.png?"+new Date().getTime());
    $("#memory").attr("src", url+"/monitor/memory.png?"+new Date().getTime());
    $("#heap").attr("src", url+"/monitor/heap.png?"+new Date().getTime());
    console.log("send request");
    }, interval, interval);
}
window.onload = () => refreshImage(interval);
