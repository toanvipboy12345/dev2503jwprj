document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input[name="quantity"]').forEach(input => {
        input.addEventListener('change', function () {
            if (this.value < 1) {
                this.value = 1;
            }
        });
    });
});