console.log("Home page loaded successfully.");

document.addEventListener("DOMContentLoaded", function() {

    // ==========================================
    // 鍒涙柊鐐?2: 灞€閮ㄧ幆澧冮煶鏁?(ASMR)
    // ==========================================
    // 閫変腑鎵€鏈夊甫鏈?data-audio 灞炴€х殑鍗＄墖
    const heritageCards = document.querySelectorAll('.heritage-card[data-audio]');

    heritageCards.forEach(card => {
        // 鑾峰彇杩欏潡鍗＄墖瀵瑰簲鐨勯煶棰?ID
        const audioId = card.getAttribute('data-audio');
        const audioElement = document.getElementById(audioId);

        if (audioElement) {
            // ASMR 鐨勭簿楂撴槸鏋佸叾杞绘煍锛屼笉瑕佸悡鍒扮敤鎴凤紝闊抽噺璁句负 20%
            audioElement.volume = 0.2; 

            // 榧犳爣绉诲叆鏃舵挱鏀?
            card.addEventListener('mouseenter', () => {
                // play() 杩斿洖涓€涓?Promise锛屽鐞嗘祻瑙堝櫒鍙兘绂佹鑷姩鎾斁鐨勬姤閿?
                audioElement.play().catch(error => {
                    console.log("娴忚鍣ㄩ樆姝簡鑷姩鎾斁锛岀敤鎴烽渶鍏堜笌椤甸潰浜や簰:", error);
                });
            });

            // 榧犳爣绉诲嚭鏃舵殏鍋滐紝骞惰闊抽杩涘害鍥炲埌璧风偣
            card.addEventListener('mouseleave', () => {
                audioElement.pause();
                // 濡傛灉浣犳兂姣忔榧犳爣鏀句笂鍘婚兘浠庡ご鎾斁锛屼繚鐣欎笅闈㈣繖琛岋細
                // audioElement.currentTime = 0; 
            });
        }
    });

     const galleryData = [
        {
            // 绗竴涓枃鐗?
            src: "../Resources/gbl/porcelain_china_vase.glb",
            poster: "../Resources/image/heritage1.JPG",  // 鏇挎崲涓轰綘绗竴涓枃浠剁殑鐪熷疄鍚嶅瓧
            title: "Blue and White Porcelain",
            desc: "An exquisite masterpiece of ancient Chinese ceramic art. Known for its underglaze blue decoration, this vase features intricate traditional patterns."
        },
        {
            // 绗竴涓枃鐗╋細鏈ㄩ洉
            src: "../Resources/gbl/wood_carving.glb",
            poster: "../Resources/image/heritage2.JPG",  // 鈿狅笍 璇风‘淇濅綘鐨勬枃浠跺悕鏄繖涓紝鎴栬€呮墜鍔ㄤ慨鏀硅繖閲?
            title: "Traditional Wood Carving",
            desc: "A masterpiece of intricate craftsmanship. Chinese wood carving blends architectural function with decorative art, featuring delicate openwork and auspicious motifs that reflect the profound aesthetic of ancient life."
        },
        {
            // 绗簩涓枃鐗╋細绾㈢伅绗?
            src: "../Resources/gbl/chinese_paper_lantern__red.glb",
            poster: "../Resources/image/heritage3.JPG",   // 鈿狅笍 璇风‘淇濅綘鐨勬枃浠跺悕鏄繖涓紝鎴栬€呮墜鍔ㄤ慨鏀硅繖閲?
            title: "Traditional Red Lantern",
            desc: "A vibrant symbol of joy, reunion, and prosperity. Crafted with bamboo frames and red silk, these lanterns have illuminated festivals for centuries, embodying the warm spirit of cultural celebration."
        }
    ];

    let currentIndex = 0; // 褰撳墠鏄剧ず鐨勫睍鍝佺储寮?

    // 2. 鑾峰彇 DOM 鍏冪礌
    const modelViewer = document.getElementById('main3DViewer');
    const titleElement = document.getElementById('modelTitle');
    const descElement = document.getElementById('modelDesc');
    const infoContainer = document.getElementById('viewerInfoText');
    const prevBtn = document.getElementById('prevModelBtn');
    const nextBtn = document.getElementById('nextModelBtn');
    const fallbackImage = document.getElementById('fallback3DImage');

    function showModelFallback(index) {
        const activeItem = galleryData[index] || galleryData[currentIndex] || galleryData[0];
        if (fallbackImage && activeItem) {
            fallbackImage.src = activeItem.poster;
            fallbackImage.alt = activeItem.title;
            fallbackImage.style.display = 'block';
        }
        if (modelViewer) {
            modelViewer.style.display = 'none';
        }
    }

    function hideModelFallback() {
        if (fallbackImage) {
            fallbackImage.style.display = 'none';
        }
        if (modelViewer) {
            modelViewer.style.display = 'block';
        }
    }

    // 3. 鏇存柊椤甸潰鍐呭鐨勫嚱鏁?(甯︽贰鍏ユ贰鍑哄姩鐢?
    function updateGallery(index) {
        // 鍏堣鏂囧瓧鍖哄彉閫忔槑锛屽埗閫犳贰鍑烘晥鏋?
        infoContainer.style.opacity = 0;

        setTimeout(() => {
            // 鏇存敼 3D 妯″瀷鐨勮矾寰?
            modelViewer.src = galleryData[index].src;
            modelViewer.removeAttribute('poster');
            if (fallbackImage) {
                fallbackImage.src = galleryData[index].poster;
                fallbackImage.alt = galleryData[index].title;
            }
            
            // 鏇存敼鏍囬鍜屾弿杩?
            titleElement.textContent = galleryData[index].title;
            descElement.textContent = galleryData[index].desc;
            
            // 鏂囧瓧鏇存柊鍚庯紝閲嶆柊鏄剧ず锛堟贰鍏ワ級
            infoContainer.style.opacity = 1;
        }, 300); // 300姣鐨勬枃瀛楀垏鎹㈠姩鐢绘椂闂?
    }

    // 4. 鐐瑰嚮涓嬩竴寮犵殑閫昏緫
    nextBtn.addEventListener('click', function() {
        currentIndex++;
        // 濡傛灉鍒颁簡鏈€鍚庝竴寮狅紝灏卞洖鍒扮涓€寮?
        if (currentIndex >= galleryData.length) {
            currentIndex = 0;
        }
        updateGallery(currentIndex);
    });

    // 5. 鐐瑰嚮涓婁竴寮犵殑閫昏緫
    prevBtn.addEventListener('click', function() {
        currentIndex--;
        // 濡傛灉鏄涓€寮犲啀寰€宸︾偣锛屽氨璺冲埌鏈€鍚庝竴寮?
        if (currentIndex < 0) {
            currentIndex = galleryData.length - 1;
        }
        updateGallery(currentIndex);
    });

     // 鑾峰彇寮圭獥鐩稿叧鍏冪礌
    const modal = document.getElementById('videoModal');
    const modalVideo = document.getElementById('modalVideo');
    const closeModal = document.querySelector('.close-modal');
    const cards = document.querySelectorAll('.heritage-card');

    // 閬嶅巻鎵€鏈夊崱鐗囷紝鐩戝惉鐐瑰嚮浜嬩欢
    cards.forEach(card => {
        card.addEventListener('click', function() {
            const videoSrc = this.getAttribute('data-video');
            
            if (videoSrc) {
                // 1. 璁剧疆瑙嗛婧?
                modalVideo.src = videoSrc;
                
                // 2. 鏄剧ず寮圭獥
                modal.style.display = 'flex';
                
                // 3. 鎾斁瑙嗛
                modalVideo.play();

                // 4. (鍙€? 濡傛灉姝ゆ椂鏈?ASMR 鍦ㄦ挱鏀撅紝鍙互灏嗗叾鏆傚仠锛岄伩鍏嶅０闊虫贩涔?
                const allAudios = document.querySelectorAll('audio');
                allAudios.forEach(a => a.pause());
            }
        });
    });

    // 鐐瑰嚮 X 鍏抽棴鎸夐挳
    closeModal.addEventListener('click', function() {
        closeVideo();
    });

    // 鐐瑰嚮鑳屾櫙澶勪篃鍙互鍏抽棴
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeVideo();
        }
    });

    // 鍏抽棴瑙嗛鐨勯€氱敤鍑芥暟
    function closeVideo() {
        modal.style.display = 'none';
        modalVideo.pause(); // 鍋滄鎾斁
        modalVideo.src = ""; // 娓呯┖婧愶紝閲婃斁鍐呭瓨
    }

});

document.addEventListener('DOMContentLoaded', () => {
    // --- 1. 瀵艰埅鏍忔粴鍔ㄤ氦浜?---
    const header = document.querySelector('header');
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    });

    // --- 2. 婊氬姩娣″叆鏄剧幇鍔ㄧ敾 (Scroll Reveal) ---
    const observerOptions = {
        threshold: 0.1
    };

    const revealObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('reveal');
                // 澧炲姞涓€鐐归殢鏈哄欢杩燂紝璁╁崱鐗囩湅璧锋潵鏄寜椤哄簭璺冲嚭鏉ョ殑
                const delay = Math.random() * 0.2;
                entry.target.style.transitionDelay = `${delay}s`;
            }
        });
    }, observerOptions);

    document.querySelectorAll('.feature-card, .heritage-card, .exhibition-3d').forEach(el => {
        revealObserver.observe(el);
    });

    // --- 3. 閬椾骇鍗＄墖 3D 纾佽创鏁堟灉 ---
    const cards = document.querySelectorAll('.heritage-card');
    cards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;
            
            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.05, 1.05, 1.05)`;
        });
        
        card.addEventListener('mouseleave', () => {
            card.style.transform = `perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)`;
        });
    });

    // --- 4. 瑙嗛寮圭獥澧炲己浜や簰 ---
    const modal = document.getElementById('videoModal');
    const cardsWithVideo = document.querySelectorAll('.heritage-card[data-video]');
    
    cardsWithVideo.forEach(card => {
        card.onclick = function() {
            const videoSrc = this.getAttribute('data-video');
            const videoPlayer = document.getElementById('modalVideo');
            videoPlayer.querySelector('source').src = videoSrc;
            videoPlayer.load();
            modal.style.display = 'flex';
            setTimeout(() => modal.classList.add('active'), 10);
        };
    });

    document.querySelector('.close-modal').onclick = () => {
        modal.classList.remove('active');
        setTimeout(() => modal.style.display = 'none', 400);
        document.getElementById('modalVideo').pause();
    };
});

(function () {
    const NOTICE_STORAGE_PREFIX = 'heritage-contributor-application-notice';
    let applicationNoticeRequest = null;

    function getCurrentSessionUser() {
        if (!window.HeritageSession || typeof window.HeritageSession.getCurrentUser !== 'function') {
            return null;
        }
        return window.HeritageSession.getCurrentUser();
    }

    function getNoticeStorageKey(userId) {
        return `${NOTICE_STORAGE_PREFIX}:${userId}`;
    }

    function buildNoticeSignature(application) {
        return [
            application.id || '',
            application.status || '',
            application.reviewedAt || ''
        ].join(':');
    }

    function hasSeenApplicationNotice(userId, application) {
        const signature = buildNoticeSignature(application);
        return localStorage.getItem(getNoticeStorageKey(userId)) === signature;
    }

    function markApplicationNoticeSeen(userId, application) {
        localStorage.setItem(getNoticeStorageKey(userId), buildNoticeSignature(application));
    }

    function removeExistingApplicationNotice() {
        const existing = document.getElementById('contributorApprovalNotice');
        if (existing) {
            existing.remove();
        }
    }

    function showApplicationNotice(application) {
        removeExistingApplicationNotice();

        const approved = application.status === 'APPROVED';
        const overlay = document.createElement('div');
        overlay.id = 'contributorApprovalNotice';
        overlay.style.position = 'fixed';
        overlay.style.inset = '0';
        overlay.style.background = 'rgba(0, 0, 0, 0.28)';
        overlay.style.display = 'flex';
        overlay.style.alignItems = 'center';
        overlay.style.justifyContent = 'center';
        overlay.style.zIndex = '9999';

        const dialog = document.createElement('div');
        dialog.style.width = 'min(520px, calc(100vw - 32px))';
        dialog.style.padding = '28px 26px';
        dialog.style.borderRadius = '18px';
        dialog.style.boxShadow = '0 18px 45px rgba(0, 0, 0, 0.22)';
        dialog.style.background = approved ? '#2E8B57' : '#C41E3A';
        dialog.style.color = '#fff';
        dialog.style.textAlign = 'left';

        const title = document.createElement('div');
        title.textContent = approved ? 'Application Approved' : 'Application Result';
        title.style.fontSize = '1.25rem';
        title.style.fontWeight = '700';
        title.style.marginBottom = '12px';

        const message = document.createElement('div');
        message.textContent = approved
            ? 'Congratulations! You have been approved as a contributor. Please feel free to share your stories with others!'
            : 'Unfortunately, you do not fully meet the requirements to become a contributor. Please do not be discouraged, the application door is always open to you!';
        message.style.fontSize = '0.98rem';
        message.style.lineHeight = '1.7';

        const buttonRow = document.createElement('div');
        buttonRow.style.display = 'flex';
        buttonRow.style.justifyContent = 'flex-end';
        buttonRow.style.marginTop = '22px';

        const confirmButton = document.createElement('button');
        confirmButton.type = 'button';
        confirmButton.textContent = 'OK';
        confirmButton.style.border = 'none';
        confirmButton.style.borderRadius = '999px';
        confirmButton.style.padding = '10px 22px';
        confirmButton.style.fontWeight = '700';
        confirmButton.style.cursor = 'pointer';
        confirmButton.style.background = '#fff';
        confirmButton.style.color = approved ? '#2E8B57' : '#C41E3A';
        confirmButton.addEventListener('click', function () {
            overlay.remove();
        });

        overlay.addEventListener('click', function (event) {
            if (event.target === overlay) {
                overlay.remove();
            }
        });

        buttonRow.appendChild(confirmButton);
        dialog.appendChild(title);
        dialog.appendChild(message);
        dialog.appendChild(buttonRow);
        overlay.appendChild(dialog);
        document.body.appendChild(overlay);
    }

    async function checkContributorApplicationNotice() {
        const user = getCurrentSessionUser();
        if (!user || !user.id) {
            return;
        }

        if (applicationNoticeRequest) {
            return applicationNoticeRequest;
        }

        applicationNoticeRequest = fetch(`/api/ly-contributor/contributor-applications/my/${user.id}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('request failed');
                }
                return response.json();
            })
            .then(function (application) {
                if (!application || (application.status !== 'APPROVED' && application.status !== 'REJECTED')) {
                    return;
                }

                if (hasSeenApplicationNotice(user.id, application)) {
                    return;
                }

                markApplicationNoticeSeen(user.id, application);
                showApplicationNotice(application);
            })
            .catch(function () {
            })
            .finally(function () {
                applicationNoticeRequest = null;
            });

        return applicationNoticeRequest;
    }

    document.addEventListener('DOMContentLoaded', function () {
        checkContributorApplicationNotice();
    });

    window.addEventListener('focus', function () {
        checkContributorApplicationNotice();
    });

    document.addEventListener('visibilitychange', function () {
        if (!document.hidden) {
            checkContributorApplicationNotice();
        }
    });
})();

