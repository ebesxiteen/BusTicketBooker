(
    function () {
        document.addEventListener("DOMContentLoaded", function () {
            sidebarController();
            navbarController();

            function navbarController() {
                // Hàm này tự động lấy URL nên không cần sửa
                // Nếu URL là /greenbus/admin thì nó sẽ tự hiện chữ greenbus
                window.addEventListener("load", pathShowControl());

                function pathShowControl() {
                    const pathShow = document.querySelector(".path-show");
                    let pathSplit = window.location.pathname.split("/");

                    pathShow.innerHTML = "";
                    pathSplit.forEach((pathComponent, index) => {
                        if (index > 0) {
                            if (index < pathSplit.length - 1) {
                                pathShow.innerHTML += `
                                <div>${pathComponent}</div>
                                <i class="fa-solid fa-angle-right h-fit"></i>
                                `;
                            } else {
                                pathShow.innerHTML += `
                                <div>${pathComponent}</div>
                                `;
                            }
                        }
                    });
                }
            }

            function sidebarController() {
                collapeControl();

                function collapeControl() {
                    const listCollapseToggle = document.querySelectorAll(".collapse-toggle");

                    listCollapseToggle.forEach(toggle => {
                        toggle.addEventListener("click", function () {
                            let targetId = toggle.getAttribute("data-greenbus-target");
                            
                            if (targetId) {
                                document.querySelector(targetId).classList.toggle("md:w-full");
                            }
                        });
                    });
                }
            }
        });
    }
)();