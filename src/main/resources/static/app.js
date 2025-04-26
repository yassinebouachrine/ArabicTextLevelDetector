document.addEventListener('DOMContentLoaded', function() {
    const analyzeForm = document.querySelector('form[action="/analyze"]');
    const textArea = document.getElementById('arabicText');
    const exampleButtons = document.querySelectorAll('[onclick^="insertExample"]');
    const loaderOverlay = document.createElement('div');

    initializeUI();

    function initializeUI() {
        setupLoadingIndicator();

        addAnimations();

        setupFormHandling();

        if (textArea) {
            addCharacterCounter();
        }

        setupTooltips();

        addSmoothScrolling();

        initializeCharts();
    }


    function setupLoadingIndicator() {
        loaderOverlay.className = 'loader-overlay';
        loaderOverlay.innerHTML = `
            <div class="text-center">
                <div class="loading-spinner"></div>
                <div class="loader-message">جاري تحليل النص...</div>
            </div>
        `;
        document.body.appendChild(loaderOverlay);

        if (analyzeForm) {
            analyzeForm.addEventListener('submit', function() {
                if (validateForm()) {
                    showLoading();
                }
            });
        }
    }

    function showLoading() {
        loaderOverlay.classList.add('active');
    }

    function hideLoading() {
        loaderOverlay.classList.remove('active');
    }

    function addAnimations() {
        document.querySelectorAll('.header, .card').forEach(function(element, index) {
            element.classList.add('fade-in');
            element.style.animationDelay = (index * 0.1) + 's';
        });
    }

    function setupFormHandling() {
        if (analyzeForm) {
            analyzeForm.addEventListener('submit', function(e) {
                if (!validateForm()) {
                    e.preventDefault();
                }
            });
        }
    }

    function validateForm() {
        if (!textArea || textArea.value.trim() === '') {
            showError('يرجى إدخال نص للتحليل');
            return false;
        }

        if (textArea.value.trim().length < 10) {
            showError('النص قصير جدًا. يرجى إدخال نص أطول للحصول على تحليل دقيق');
            return false;
        }

        if (!containsArabic(textArea.value)) {
            showError('يرجى إدخال نص باللغة العربية');
            return false;
        }

        return true;
    }

    function containsArabic(text) {
        const arabicPattern = /[\u0600-\u06FF\u0750-\u077F]/;
        return arabicPattern.test(text);
    }

    function showError(message) {
        const existingError = document.querySelector('.alert-danger');
        if (existingError) {
            existingError.remove();
        }

        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger';
        errorDiv.textContent = message;

        const submitButton = analyzeForm.querySelector('button[type="submit"]').parentNode;
        analyzeForm.insertBefore(errorDiv, submitButton);

        errorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function addCharacterCounter() {
        const counter = document.createElement('div');
        counter.className = 'text-muted text-end small mt-1';
        counter.id = 'textCounter';
        updateCounter(counter, textArea.value);

        textArea.parentNode.insertBefore(counter, textArea.nextSibling);

        textArea.addEventListener('input', function() {
            updateCounter(counter, this.value);
        });
    }


    function updateCounter(counterElement, text) {
        const charCount = text.trim().length;
        const wordCount = text.trim() ? text.trim().split(/\s+/).length : 0;

        counterElement.textContent = `${charCount} حرف | ${wordCount} كلمة`;
    }


    function setupTooltips() {
        const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
        if (tooltipTriggerList.length > 0) {
            const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl =>
                new bootstrap.Tooltip(tooltipTriggerEl));
        }

        document.querySelectorAll('.list-group-item').forEach(item => {
            if (item.querySelector('[data-bs-toggle="tooltip"]')) return;

            if (item.textContent.includes('متوسط طول الجمل')) {
                addTooltipToItem(item, 'مؤشر على تعقيد الجمل - الجمل الأطول عادة ما تكون أكثر تعقيدًا');
            } else if (item.textContent.includes('متوسط طول الكلمات')) {
                addTooltipToItem(item, 'مؤشر على صعوبة المفردات - الكلمات الأطول غالبًا ما تكون أكثر تخصصًا أو تعقيدًا');
            } else if (item.textContent.includes('تنوع المفردات')) {
                addTooltipToItem(item, 'نسبة الكلمات الفريدة إلى إجمالي الكلمات - النسبة العالية تشير إلى نص أكثر تعقيدًا');
            } else if (item.textContent.includes('نسبة الكلمات الطويلة')) {
                addTooltipToItem(item, 'نسبة الكلمات التي تزيد عن 6 أحرف - يشير إلى استخدام مصطلحات أكثر تخصصًا');
            } else if (item.textContent.includes('نسبة تجمعات الحروف الصعبة')) {
                addTooltipToItem(item, 'تجمعات الحروف الساكنة التي قد تجعل النطق أكثر صعوبة');
            }
        });
    }

    function addTooltipToItem(item, tooltipText) {
        const infoIcon = document.createElement('i');
        infoIcon.className = 'fas fa-info-circle ms-2';
        infoIcon.setAttribute('data-bs-toggle', 'tooltip');
        infoIcon.setAttribute('data-bs-placement', 'top');
        infoIcon.setAttribute('title', tooltipText);

        if (document.dir === 'rtl') {
            item.insertBefore(infoIcon, item.firstChild);
        } else {
            item.appendChild(infoIcon);
        }

        new bootstrap.Tooltip(infoIcon);
    }

    function addSmoothScrolling() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function(e) {
                e.preventDefault();
                const targetId = this.getAttribute('href');
                if (targetId !== '#') {
                    const targetElement = document.querySelector(targetId);
                    if (targetElement) {
                        targetElement.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start'
                        });
                    }
                }
            });
        });
    }

    function initializeCharts() {
        const confidenceChart = document.getElementById('confidenceChart');


        if (confidenceChart) {
            customizeConfidenceChart();
        }
    }


    window.insertExample = function(button) {
        const text = button.previousElementSibling.textContent;
        textArea.value = text;
        textArea.focus();

        const counter = document.getElementById('textCounter');
        if (counter) {
            updateCounter(counter, text);
        }

        textArea.classList.add('border-success');
        setTimeout(() => {
            textArea.classList.remove('border-success');
        }, 1000);

        textArea.scrollIntoView({ behavior: 'smooth', block: 'center' });
    };


    function addFontAwesome() {
        const fontAwesomeScript = document.createElement('script');
        fontAwesomeScript.src = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/js/all.min.js';
        fontAwesomeScript.integrity = 'sha512-fD9DI5bZwQxOi7MhYWnnNPlvXdp/2Pj3XSTRrFs5FQa4mizyGLnJcN6tuvUS6LbmgN1ut+XGSABKvjN0H6Aoow==';
        fontAwesomeScript.crossOrigin = 'anonymous';
        fontAwesomeScript.referrerPolicy = 'no-referrer';
        document.head.appendChild(fontAwesomeScript);
    }

    addFontAwesome();
});
