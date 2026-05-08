document.addEventListener("DOMContentLoaded", function() {

    // 1. 内容淡入滑入效果 (Reveal on Scroll)
    const reveals = document.querySelectorAll(".reveal");
    
    const revealCallback = (entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add("active");
            }
        });
    };

    const observer = new IntersectionObserver(revealCallback, {
        threshold: 0.1
    });

    reveals.forEach(el => observer.observe(el));

    // 2. 数字跳动动画 (Count Up)
    const stats = document.querySelectorAll(".stat-number");
    const speed = 200;

    const runCountUp = (targetEl) => {
        const updateCount = () => {
            const target = +targetEl.getAttribute("data-target");
            const count = +targetEl.innerText;
            const inc = target / speed;

            if (count < target) {
                targetEl.innerText = Math.ceil(count + inc);
                setTimeout(updateCount, 15);
            } else {
                targetEl.innerText = target;
            }
        };
        updateCount();
    };

    // 监听滚动到数字区域才触发
    const statsSection = document.querySelector(".impact-section");
    const statsObserver = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
            stats.forEach(s => runCountUp(s));
            statsObserver.unobserve(statsSection); // 只运行一次
        }
    }, { threshold: 0.6 });

    if(statsSection) statsObserver.observe(statsSection);

    // 3. 给 Pillar Card 增加轻微的 3D 悬浮偏移
    const cards = document.querySelectorAll(".pillar-card");
    cards.forEach(card => {
        card.addEventListener("mousemove", (e) => {
            let x = (window.innerWidth / 2 - e.pageX) / 50;
            let y = (window.innerHeight / 2 - e.pageY) / 50;
            card.style.transform = `translateY(-15px) rotateY(${x}deg) rotateX(${y}deg)`;
        });
        card.addEventListener("mouseleave", () => {
            card.style.transform = `translateY(0) rotateY(0) rotateX(0)`;
        });
    });
});