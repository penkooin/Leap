
const interval = '5000';

function refreshImage(interval) {
  setInterval( function() {
    $("#cpu").attr("src", "/monitor/cpu.png?"+new Date().getTime());
    $("#thread").attr("src", "/monitor/thread.png?"+new Date().getTime());
    $("#memory").attr("src", "/monitor/memory.png?"+new Date().getTime());
    $("#heap").attr("src", "/monitor/heap.png?"+new Date().getTime());
    console.log("send request");
    }, interval, interval);
}
window.onload = () => refreshImage(interval);
