(function () {
    document.addEventListener("DOMContentLoaded", function () {
        // start
        // modalController(); // Uncomment if you enable the modal logic later
        deleteController();
        getDetailsController();
        searchController();

        function deleteController() {
            const deleteBtn = document.querySelectorAll(".delete-btn");
            deleteBtn.forEach(btn => {
                btn.addEventListener("click", function () {
                    console.log(btn.dataset.id);
                    // Fixed typo in message
                    const message = `Bạn có chắc muốn xóa tuyến đường ${btn.dataset.id} không?`;
                    
                    if (confirm(message)) {
                        // FIXED: Use relative URL
                        fetch("/admin/routes/delete", {
                            method: "DELETE",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({
                                routeId: btn.dataset.id
                            })
                        })
                        .then(response => {
                            if (!response.ok) {
                                throw new Error('Delete failed');
                            }
                            return response.json();
                        })
                        .then(data => {
                            if (data === true) {
                                alert("Delete successfully");
                                // Reload page or remove element from DOM
                                window.location.href = "/admin/routes";
                                // Alternatively, remove the row without reload:
                                // btn.closest("tr").remove(); 
                            } else {
                                alert("Some error while deleting");
                            }
                        })
                        .catch(error => {
                            alert("Delete failed: " + error);
                        });
                    }
                });
            });
        }

        function getDetailsController() {
            const detailsBtn = document.querySelectorAll(".update-btn");
            detailsBtn.forEach(btn => {
                btn.addEventListener("click", function () {
                    window.location.href = "/admin/routes/updating/" + btn.dataset.id;
                });
            });
        }

        function searchController() {
            const searchBox = document.querySelector(".search-box");
            const searchContainer = document.getElementById("search-result-collapse");
            let timeout;

            if (!searchBox) return; // Guard clause if element doesn't exist

            searchBox.addEventListener("input", function () {
                clearTimeout(timeout);
                timeout = setTimeout(() => {
                    const searchTerm = searchBox.value.trim();

                    // Logic to show/hide the results container
                    if (!searchContainer.classList.contains("show") && searchTerm !== "") {
                        searchContainer.classList.remove("hidden");
                        searchContainer.classList.add("show"); // Assuming you have CSS for .show
                    } else if (searchContainer.classList.contains("show") && searchTerm === "") {
                        searchContainer.classList.remove("show");
                        searchContainer.classList.add("hidden");
                        searchContainer.innerHTML = ""; // Clear results when empty
                        return; // Stop here if empty
                    }

                    // FIXED: Use relative URL
                    fetch("/admin/routes/search", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            name: searchTerm
                        })
                    })
                    .then(response => response.json())
                    .then(response => {
                        // FIXED: Clear previous results before adding new ones
                        searchContainer.innerHTML = "";

                        if (response.list && response.list.length > 0) {
                            response.list.forEach(route => {
                                // Added click event to redirect to details on click (Optional but UX friendly)
                                searchContainer.innerHTML += `
                                <div class="user-card p-2 border-b hover:bg-gray-100 cursor-pointer" onclick="window.location.href='/admin/routes/updating/${route.routeId}'">
                                    <div class="font-bold">Departure: ${route.departureLocation}</div>
                                    <div class="font-bold">Arrival: ${route.arrivalLocation}</div>
                                    <div class="text-sm">Time estimate: ${route.estimatedTime}</div>
                                    <div class="text-sm text-gray-500">Status: ${route.routeStatus}</div>
                                </div>
                                `;
                            });
                        } else {
                            searchContainer.innerHTML = '<div class="p-2 text-gray-500">No routes found</div>';
                        }
                    })
                    .catch(error => {
                        console.error("Request failed: ", error);
                        // alert("Request failed: " + error); // Alert might be annoying while typing
                    });
                }, 500); // Reduced delay to 500ms for snappier feel
            });
            
            // Optional: Hide search results when clicking outside
            document.addEventListener("click", function(e) {
                if (!searchBox.contains(e.target) && !searchContainer.contains(e.target)) {
                    searchContainer.classList.add("hidden");
                    searchContainer.classList.remove("show");
                }
            });
        }

        // Modal controller code commented out as in original...
    });
})();