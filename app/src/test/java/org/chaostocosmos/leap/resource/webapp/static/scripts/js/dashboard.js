document.addEventListener('DOMContentLoaded', function() {
    var systemStatusChart = generateSystemStatusChart();
    var accumulatedData = [];

    updateSystemStatusChart(systemStatusChart, accumulatedData);
});

function generateSystemStatusChart() {
    var systemStatusData = {
        labels: [],
        datasets: [
            {
                label: 'Memory Usage',
                data: [],
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            },
            {
                label: 'Free Memory',
                data: [],
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }
        ]
    };

    var systemStatusOptions = {
        responsive: true,
        scales: {
            y: {
                beginAtZero: true,
                grace: '60%'
            }
        }
    };

    var systemStatusChartElement = document.getElementById('systemStatusChart');
    systemStatusChartElement.style.width = '1000px';
    systemStatusChartElement.style.height = '300px';

    var systemStatusChart = new Chart(systemStatusChartElement, {
        type: 'line',
        data: systemStatusData,
        options: systemStatusOptions
    });

    return systemStatusChart;
}

function updateSystemStatusChart(systemStatusChart, accumulatedData) {
    setInterval(function() {
        fetch('http://localhost:8080/chatgpt/memory')
            .then(response => response.json())
            .then(data => {
                // Accumulate the new data with the previously stored data
                accumulatedData.push(data);

                // Limit the number of stored data points
                var maxDataPoints = 24;
                if (accumulatedData.length > maxDataPoints) {
                    accumulatedData.shift(); // Remove the oldest data point
                }

                // Update the chart labels and data with the accumulated data
                var labels = accumulatedData.map(item => item.labels).flat();
                var usageData = accumulatedData.map(item => item.usage).flat();
                var freeData = accumulatedData.map(item => item.free).flat();
                systemStatusChart.data.labels = labels;
                systemStatusChart.data.datasets[0].data = usageData;
                systemStatusChart.data.datasets[1].data = freeData;
                systemStatusChart.update();
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });
    }, 3000);
}


