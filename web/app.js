/**
 * Cinema Booking System - Frontend Application Logic
 * Pure Vanilla JavaScript (No Frameworks)
 */

document.addEventListener('DOMContentLoaded', () => {
    // === APPLICATION STATE ===
    const state = {
        movies: [],
        customers: [],
        showtimes: [],
        currentShowtime: null,
        selectedSeat: null,
        tickets: [],
        unpaidTickets: [],
        selectedUnpaidTicket: null,
        activeTab: 'tab-movies',
        movieFilter: 'all',
        ticketFilter: 'all'
    };

    // === DOM ELEMENTS ===
    const navButtons = document.querySelectorAll('.nav-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');
    const toastContainer = document.getElementById('toast-container');

    // Tab 1: Movies
    const moviesGrid = document.getElementById('movies-grid');
    const movieSearchInput = document.getElementById('movie-search-input');
    const movieFilterButtons = document.querySelectorAll('[data-filter]');

    // Tab 2: Booking
    const bookingCustSelect = document.getElementById('booking-customer-select');
    const bookingMovieSelect = document.getElementById('booking-movie-select');
    const bookingShowtimeSelect = document.getElementById('booking-showtime-select');
    const seatMap = document.getElementById('seat-map');
    const availableSeatsBadge = document.getElementById('available-seats-badge');
    const pricingBreakdown = document.getElementById('pricing-breakdown');
    const summaryDetails = document.getElementById('summary-details');
    const btnConfirmBooking = document.getElementById('btn-confirm-booking');

    // Tab 3: Payment
    const unpaidTicketsTable = document.querySelector('#unpaid-tickets-table tbody');
    const unpaidBadge = document.getElementById('unpaid-badge');
    const paymentTicketBox = document.getElementById('payment-ticket-box');
    const paymentMethodsGroup = document.getElementById('payment-methods-group');
    const btnExecutePayment = document.getElementById('btn-execute-payment');
    const btnRefreshUnpaid = document.getElementById('btn-refresh-unpaid');

    // Tab 4: Customers
    const customersTable = document.querySelector('#customers-table tbody');
    const customerTotalCount = document.getElementById('customer-total-count');
    const addCustomerForm = document.getElementById('add-customer-form');

    // Tab 5: Tickets & Revenue
    const statTotalTickets = document.getElementById('stat-total-tickets');
    const statPaidTickets = document.getElementById('stat-paid-tickets');
    const statUnpaidTickets = document.getElementById('stat-unpaid-tickets');
    const statTotalRevenue = document.getElementById('stat-total-revenue');
    const allTicketsTable = document.querySelector('#all-tickets-table tbody');
    const ticketFilterButtons = document.querySelectorAll('[data-ticket-filter]');

    // Modals
    const ticketModal = document.getElementById('ticket-modal');
    const ticketReceiptContent = document.getElementById('ticket-receipt-content');
    const btnCloseTicketModal = document.getElementById('btn-close-ticket-modal');
    const btnPayNowModal = document.getElementById('btn-pay-now-modal');

    const quickCustModal = document.getElementById('quick-cust-modal');
    const btnQuickAddCust = document.getElementById('btn-quick-add-cust');
    const btnCloseQuickCust = document.getElementById('btn-close-quick-cust');
    const btnCancelQuickCust = document.getElementById('btn-cancel-quick-cust');
    const quickCustForm = document.getElementById('quick-cust-form');

    let latestBookedTicket = null;

    // ==============================================================
    // INITIALIZATION & TAB ROUTING
    // ==============================================================
    function init() {
        setupNavigation();
        loadMovies();
        loadCustomers();
        loadTickets();

        // Event listeners
        movieSearchInput.addEventListener('input', renderMovies);
        movieFilterButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                movieFilterButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                state.movieFilter = btn.dataset.filter;
                renderMovies();
            });
        });

        // Booking selectors
        bookingCustSelect.addEventListener('change', onCustomerOrShowtimeChange);
        bookingMovieSelect.addEventListener('change', onMovieSelectChange);
        bookingShowtimeSelect.addEventListener('change', onShowtimeSelectChange);
        btnConfirmBooking.addEventListener('click', onConfirmBooking);

        // Payment
        btnRefreshUnpaid.addEventListener('click', loadTickets);
        btnExecutePayment.addEventListener('click', onExecutePayment);
        document.querySelectorAll('input[name="payMethod"]').forEach(radio => {
            radio.addEventListener('change', (e) => {
                document.querySelectorAll('.payment-method-item').forEach(item => item.classList.remove('active'));
                radio.closest('.payment-method-item').classList.add('active');
            });
        });

        // Add Customer Form
        addCustomerForm.addEventListener('submit', onAddCustomer);

        // Quick Customer Modal
        btnQuickAddCust.addEventListener('click', () => quickCustModal.classList.add('active'));
        btnCloseQuickCust.addEventListener('click', () => quickCustModal.classList.remove('active'));
        btnCancelQuickCust.addEventListener('click', () => quickCustModal.classList.remove('active'));
        quickCustForm.addEventListener('submit', onQuickAddCustomer);

        // Ticket Receipt Modal
        btnCloseTicketModal.addEventListener('click', () => ticketModal.classList.remove('active'));
        btnPayNowModal.addEventListener('click', () => {
            ticketModal.classList.remove('active');
            switchTab('tab-payment');
            if (latestBookedTicket) {
                selectUnpaidTicket(latestBookedTicket.id);
            }
        });

        // Ticket Filter
        ticketFilterButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                ticketFilterButtons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                state.ticketFilter = btn.dataset.ticketFilter;
                renderAllTickets();
            });
        });
    }

    function setupNavigation() {
        navButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const targetTab = btn.dataset.tab;
                switchTab(targetTab);
            });
        });
    }

    function switchTab(tabId) {
        state.activeTab = tabId;
        navButtons.forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tabId);
        });
        tabPanes.forEach(pane => {
            pane.classList.toggle('active', pane.id === tabId);
        });

        // Auto refresh tab specific data
        if (tabId === 'tab-movies') loadMovies();
        if (tabId === 'tab-customers') loadCustomers();
        if (tabId === 'tab-payment' || tabId === 'tab-tickets') loadTickets();
    }

    // ==============================================================
    // TAB 1: MOVIES
    // ==============================================================
    async function loadMovies() {
        try {
            const res = await fetch('/api/movies');
            state.movies = await res.json();
            updateMovieCounts();
            renderMovies();
            populateBookingMovies();
        } catch (err) {
            showToast('Lỗi nạp danh sách phim: ' + err.message, 'error');
        }
    }

    function updateMovieCounts() {
        const total = state.movies.length;
        const showing = state.movies.filter(m => m.isShowing).length;
        const stopped = total - showing;

        document.getElementById('count-all-movies').textContent = total;
        document.getElementById('count-showing-movies').textContent = showing;
        document.getElementById('count-stopped-movies').textContent = stopped;
    }

    function renderMovies() {
        const query = movieSearchInput.value.toLowerCase().trim();
        let filtered = state.movies;

        if (state.movieFilter === 'showing') {
            filtered = filtered.filter(m => m.isShowing);
        } else if (state.movieFilter === 'stopped') {
            filtered = filtered.filter(m => !m.isShowing);
        }

        if (query) {
            filtered = filtered.filter(m => 
                m.title.toLowerCase().includes(query) || 
                m.genre.toLowerCase().includes(query)
            );
        }

        if (filtered.length === 0) {
            moviesGrid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 50px 20px; color: #64748B;">
                    Không tìm thấy bộ phim nào phù hợp.
                </div>
            `;
            return;
        }

        moviesGrid.innerHTML = filtered.map(m => {
            const ageText = m.ageLimit === 0 ? 'Mọi lứa tuổi' : `${m.ageLimit}+`;
            const badgeClass = m.isShowing ? 'badge-showing' : 'badge-stopped';
            const actionBtn = m.isShowing 
                ? `<button class="btn btn-primary btn-block btn-sm" onclick="window.selectMovieForBooking('${m.id}')">
                     <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                       <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                       <line x1="16" y1="2" x2="16" y2="6"></line>
                       <line x1="8" y1="2" x2="8" y2="6"></line>
                     </svg>
                     <span>Đặt vé phim này</span>
                   </button>`
                : `<button class="btn btn-secondary btn-block btn-sm" disabled style="opacity: 0.6; cursor: not-allowed;">
                     <span>Đã ngừng chiếu</span>
                   </button>`;

            return `
                <div class="movie-card ${m.isShowing ? '' : 'stopped'}">
                    <div>
                        <div class="movie-card-header">
                            <span class="movie-badge-code">${m.id}</span>
                            <span class="badge ${badgeClass}">${m.statusName}</span>
                        </div>
                        <h3 class="movie-title">${escapeHtml(m.title)}</h3>
                        <div class="movie-meta-list">
                            <div class="movie-meta-item">
                                <strong>Thể loại:</strong>
                                <span>${escapeHtml(m.genre)}</span>
                            </div>
                            <div class="movie-meta-item">
                                <strong>Thời lượng:</strong>
                                <span>${m.duration} phút</span>
                            </div>
                            <div class="movie-meta-item">
                                <strong>Độ tuổi:</strong>
                                <span>${ageText}</span>
                            </div>
                        </div>
                    </div>
                    <div class="movie-card-action">
                        ${actionBtn}
                    </div>
                </div>
            `;
        }).join('');
    }

    window.selectMovieForBooking = function(movieId) {
        switchTab('tab-booking');
        bookingMovieSelect.value = movieId;
        onMovieSelectChange();
    };

    function populateBookingMovies() {
        const showingMovies = state.movies.filter(m => m.isShowing);
        bookingMovieSelect.innerHTML = '<option value="">-- Chọn phim xem --</option>' + 
            showingMovies.map(m => `<option value="${m.id}">${m.id} - ${escapeHtml(m.title)} (${m.duration}p)</option>`).join('');
    }

    // ==============================================================
    // TAB 2: BOOKING & 44-SEAT CINEMA HALL
    // ==============================================================
    async function onMovieSelectChange() {
        const movieId = bookingMovieSelect.value;
        bookingShowtimeSelect.innerHTML = '<option value="">-- Chọn suất chiếu --</option>';
        bookingShowtimeSelect.disabled = true;
        resetSeatMap();

        if (!movieId) return;

        try {
            const res = await fetch(`/api/showtimes?movieId=${movieId}`);
            const showtimes = await res.json();
            if (showtimes.length === 0) {
                bookingShowtimeSelect.innerHTML = '<option value="">(Phim chưa có suất chiếu)</option>';
                return;
            }

            bookingShowtimeSelect.innerHTML = '<option value="">-- Chọn suất chiếu --</option>' + 
                showtimes.map(st => `
                    <option value="${st.id}">
                        ${st.id} | ${st.roomName} | ${st.startTime} (Giá gốc: ${formatVND(st.basePrice)} - Trống ${st.availableSeats}/${st.totalSeats})
                    </option>
                `).join('');
            bookingShowtimeSelect.disabled = false;
        } catch (err) {
            showToast('Lỗi tải suất chiếu: ' + err.message, 'error');
        }
    }

    async function onShowtimeSelectChange() {
        const showtimeId = bookingShowtimeSelect.value;
        resetSeatMap();
        if (!showtimeId) return;

        try {
            const res = await fetch(`/api/showtimes/${showtimeId}/seats`);
            const data = await res.json();
            if (data.error) {
                showToast(data.error, 'error');
                return;
            }
            state.currentShowtime = data;
            renderSeatMap(data);
        } catch (err) {
            showToast('Lỗi tải sơ đồ ghế: ' + err.message, 'error');
        }
    }

    function resetSeatMap() {
        state.selectedSeat = null;
        state.currentShowtime = null;
        availableSeatsBadge.textContent = 'Trống: 0/44';
        pricingBreakdown.style.display = 'none';
        summaryDetails.innerHTML = `
            <div class="empty-selection-notice">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <p>Chọn khách hàng, suất chiếu và nhấp một ghế trên sơ đồ để xem tính giá chi tiết.</p>
            </div>
        `;
        seatMap.innerHTML = '<div class="map-placeholder">Vui lòng chọn Suất chiếu ở trên để hiển thị sơ đồ 44 ghế phòng chiếu.</div>';
    }

    function renderSeatMap(showtime) {
        availableSeatsBadge.textContent = `Trống: ${showtime.availableSeats}/${showtime.totalSeats}`;
        
        // Group seats by row (A, B, C, D, E, F)
        const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
        const seatsByRow = {};
        rows.forEach(r => seatsByRow[r] = []);

        showtime.seats.forEach(s => {
            const r = s.row;
            if (seatsByRow[r]) seatsByRow[r].push(s);
        });

        let html = '';
        rows.forEach(r => {
            const rowSeats = seatsByRow[r];
            if (!rowSeats || rowSeats.length === 0) return;

            let leftCluster = [];
            let rightCluster = [];

            if (r === 'F') {
                // Couple seats (4 seats: F1, F2 left, F3, F4 right)
                leftCluster = rowSeats.slice(0, 2);
                rightCluster = rowSeats.slice(2, 4);
            } else {
                // Rows A-E: 8 seats (4 left, 4 right)
                leftCluster = rowSeats.slice(0, 4);
                rightCluster = rowSeats.slice(4, 8);
            }

            const renderCluster = (cluster) => cluster.map(seat => {
                let seatClass = 'seat-node';
                if (seat.type === 'NORMAL') seatClass += ' seat-normal';
                else if (seat.type === 'VIP') seatClass += ' seat-vip';
                else if (seat.type === 'COUPLE') seatClass += ' seat-couple';

                if (!seat.isAvailable) seatClass += ' seat-booked';

                const isSelected = state.selectedSeat && state.selectedSeat.number === seat.number;
                if (isSelected) seatClass += ' seat-selected';

                return `
                    <div class="${seatClass}" 
                         data-seat-number="${seat.number}" 
                         data-seat-id="${seat.id}"
                         onclick="window.onSeatClick('${seat.number}')"
                         title="${seat.number} (${seat.typeName}) - ${seat.isAvailable ? 'Còn trống' : 'Đã đặt'}">
                        ${seat.number}
                    </div>
                `;
            }).join('');

            html += `
                <div class="seat-row">
                    <span class="row-label">${r}</span>
                    <div class="seats-cluster">${renderCluster(leftCluster)}</div>
                    <div class="aisle-gap"></div>
                    <div class="seats-cluster">${renderCluster(rightCluster)}</div>
                    <span class="row-label">${r}</span>
                </div>
            `;
        });

        seatMap.innerHTML = html;
    }

    window.onSeatClick = function(seatNumber) {
        if (!state.currentShowtime) return;
        const seat = state.currentShowtime.seats.find(s => s.number === seatNumber);
        if (!seat) return;

        if (!seat.isAvailable) {
            showToast(`Ghế ${seatNumber} đã có người đặt! Vui lòng chọn ghế khác.`, 'error');
            return;
        }

        const customerId = bookingCustSelect.value;
        if (!customerId) {
            showToast('Vui lòng chọn Khách hàng ở bước 1 trước khi chọn ghế!', 'error');
            bookingCustSelect.focus();
            return;
        }

        state.selectedSeat = seat;

        // Re-render seat classes to reflect selection
        document.querySelectorAll('.seat-node').forEach(node => {
            node.classList.remove('seat-selected');
            if (node.dataset.seatNumber === seatNumber) {
                node.classList.add('seat-selected');
            }
        });

        // Call Price Preview API
        fetchPricePreview(customerId, state.currentShowtime.id, seat.number);
    };

    function onCustomerOrShowtimeChange() {
        if (state.selectedSeat && state.currentShowtime && bookingCustSelect.value) {
            fetchPricePreview(bookingCustSelect.value, state.currentShowtime.id, state.selectedSeat.number);
        }
    }

    async function fetchPricePreview(customerId, showtimeId, seatNumber) {
        try {
            const res = await fetch('/api/bookings/preview', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ customerId, showtimeId, seatNumber })
            });
            const data = await res.json();
            if (!res.ok || data.error) {
                showToast(data.error || 'Lỗi tính giá vé', 'error');
                return;
            }

            renderPricePreview(data);
        } catch (err) {
            showToast('Lỗi xem trước giá: ' + err.message, 'error');
        }
    }

    function renderPricePreview(preview) {
        summaryDetails.innerHTML = `
            <div class="summary-item-box">
                <div class="summary-row">
                    <span>Khách hàng:</span>
                    <span>${escapeHtml(preview.customerName)} (${preview.customerTypeName})</span>
                </div>
                <div class="summary-row">
                    <span>Phim:</span>
                    <span>${escapeHtml(preview.movieTitle)}</span>
                </div>
                <div class="summary-row">
                    <span>Suất chiếu:</span>
                    <span>${preview.startTime}</span>
                </div>
                <div class="summary-row">
                    <span>Phòng:</span>
                    <span>${escapeHtml(preview.roomName)}</span>
                </div>
                <div class="summary-row">
                    <span>Ghế chọn:</span>
                    <span class="text-blue">${preview.seatNumber} (${preview.seatTypeName})</span>
                </div>
            </div>
        `;

        document.getElementById('val-base-price').textContent = formatVND(preview.basePrice);
        document.getElementById('val-seat-type').textContent = preview.seatTypeName;
        document.getElementById('val-surcharge').textContent = '+' + formatVND(preview.surcharge);
        document.getElementById('val-policy-name').textContent = preview.policyName;
        document.getElementById('val-final-price').textContent = formatVND(preview.finalPrice);

        pricingBreakdown.style.display = 'block';
    }

    async function onConfirmBooking() {
        const customerId = bookingCustSelect.value;
        const showtimeId = bookingShowtimeSelect.value;
        if (!customerId || !showtimeId || !state.selectedSeat) {
            showToast('Vui lòng chọn đủ Khách hàng, Suất chiếu và Ghế!', 'error');
            return;
        }

        try {
            btnConfirmBooking.disabled = true;
            btnConfirmBooking.innerHTML = '<span>Đang xử lý đặt vé...</span>';

            const res = await fetch('/api/bookings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    customerId: customerId,
                    showtimeId: showtimeId,
                    seatNumber: state.selectedSeat.number
                })
            });

            const data = await res.json();
            if (!res.ok || !data.success) {
                // Bắt BookingException từ server và hiển thị bằng toast tiếng Việt
                showToast(data.error || 'Đặt vé thất bại!', 'error');
                return;
            }

            showToast('Đặt vé thành công!', 'success');
            latestBookedTicket = data.ticket;
            openTicketReceiptModal(data.ticket);

            // Reload seat map & reload tickets
            await onShowtimeSelectChange();
            loadTickets();

        } catch (err) {
            showToast('Lỗi kết nối máy chủ: ' + err.message, 'error');
        } finally {
            btnConfirmBooking.disabled = false;
            btnConfirmBooking.innerHTML = `
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                <span>Xác Nhận Đặt Vé</span>
            `;
        }
    }

    function openTicketReceiptModal(ticket) {
        ticketReceiptContent.innerHTML = `
            <div class="ticket-field">
                <span>Mã vé:</span>
                <span class="text-blue" style="font-size: 1.05rem;">${ticket.id}</span>
            </div>
            <div class="ticket-field">
                <span>Khách hàng:</span>
                <span>${escapeHtml(ticket.customerName)} (${ticket.customerTypeName})</span>
            </div>
            <div class="ticket-divider"></div>
            <div class="ticket-field">
                <span>Phim:</span>
                <span>${escapeHtml(ticket.movieTitle)}</span>
            </div>
            <div class="ticket-field">
                <span>Phòng chiếu:</span>
                <span>${escapeHtml(ticket.roomName)}</span>
            </div>
            <div class="ticket-field">
                <span>Suất chiếu:</span>
                <span>${ticket.startTime}</span>
            </div>
            <div class="ticket-field">
                <span>Vị trí ghế:</span>
                <span class="text-blue">${ticket.seatNumber} (${ticket.seatTypeName})</span>
            </div>
            <div class="ticket-divider"></div>
            <div class="ticket-field">
                <span>Tổng tiền:</span>
                <span style="font-size: 1.15rem; color: #2563EB;">${formatVND(ticket.finalPrice)}</span>
            </div>
            <div class="ticket-field">
                <span>Trạng thái:</span>
                <span class="badge ${ticket.paid ? 'badge-showing' : 'badge-stopped'}">
                    ${ticket.paid ? 'Đã thanh toán' : 'Chưa thanh toán'}
                </span>
            </div>
        `;
        ticketModal.classList.add('active');
    }

    // ==============================================================
    // TAB 3: PAYMENT
    // ==============================================================
    function renderUnpaidTickets() {
        state.unpaidTickets = state.tickets.filter(t => !t.paid);
        unpaidBadge.textContent = state.unpaidTickets.length;
        unpaidBadge.style.display = state.unpaidTickets.length > 0 ? 'inline-block' : 'none';

        if (state.unpaidTickets.length === 0) {
            unpaidTicketsTable.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 40px; color: #64748B;">
                        Tuyệt vời! Không có vé nào chưa thanh toán.
                    </td>
                </tr>
            `;
            paymentTicketBox.innerHTML = `
                <div class="empty-selection-notice">
                    <p>Không có vé nào cần thanh toán.</p>
                </div>
            `;
            paymentMethodsGroup.style.display = 'none';
            return;
        }

        unpaidTicketsTable.innerHTML = state.unpaidTickets.map(t => {
            const isSelected = state.selectedUnpaidTicket && state.selectedUnpaidTicket.id === t.id;
            return `
                <tr class="clickable ${isSelected ? 'selected' : ''}" onclick="window.selectUnpaidTicket('${t.id}')">
                    <td><strong class="text-blue">${t.id}</strong></td>
                    <td>${escapeHtml(t.customerName)}</td>
                    <td>${escapeHtml(t.movieTitle)}</td>
                    <td>${t.startTime}</td>
                    <td><span class="badge badge-dark">${t.seatNumber}</span></td>
                    <td><strong>${formatVND(t.finalPrice)}</strong></td>
                    <td>
                        <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); window.selectUnpaidTicket('${t.id}')">
                            Chọn thanh toán
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    window.selectUnpaidTicket = function(ticketId) {
        const ticket = state.tickets.find(t => t.id === ticketId);
        if (!ticket) return;

        state.selectedUnpaidTicket = ticket;
        renderUnpaidTickets();

        paymentTicketBox.innerHTML = `
            <div class="summary-item-box" style="margin-bottom: 0;">
                <div class="summary-row">
                    <span>Mã vé:</span>
                    <strong class="text-blue">${ticket.id}</strong>
                </div>
                <div class="summary-row">
                    <span>Khách hàng:</span>
                    <span>${escapeHtml(ticket.customerName)}</span>
                </div>
                <div class="summary-row">
                    <span>Phim:</span>
                    <span>${escapeHtml(ticket.movieTitle)}</span>
                </div>
                <div class="summary-row">
                    <span>Suất chiếu:</span>
                    <span>${ticket.startTime} (${ticket.roomName})</span>
                </div>
                <div class="summary-row">
                    <span>Ghế:</span>
                    <span>${ticket.seatNumber} (${ticket.seatTypeName})</span>
                </div>
                <div class="summary-row" style="margin-top: 8px; padding-top: 8px; border-top: 1px dashed #CBD5E1;">
                    <span style="font-weight: 700; color: #0F172A;">Số tiền thanh toán:</span>
                    <span style="font-size: 1.15rem; font-weight: 800; color: #2563EB;">${formatVND(ticket.finalPrice)}</span>
                </div>
            </div>
        `;
        paymentMethodsGroup.style.display = 'flex';
    };

    async function onExecutePayment() {
        if (!state.selectedUnpaidTicket) {
            showToast('Vui lòng chọn vé cần thanh toán!', 'error');
            return;
        }

        const selectedRadio = document.querySelector('input[name="payMethod"]:checked');
        const paymentMethod = selectedRadio ? selectedRadio.value : 'CASH';

        try {
            btnExecutePayment.disabled = true;
            btnExecutePayment.innerHTML = '<span>Đang xử lý thanh toán...</span>';

            const res = await fetch('/api/tickets/pay', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ticketId: state.selectedUnpaidTicket.id,
                    paymentMethod: paymentMethod
                })
            });

            const data = await res.json();
            if (!res.ok || !data.success) {
                showToast(data.error || 'Thanh toán thất bại!', 'error');
                return;
            }

            showToast(data.message || 'Thanh toán thành công!', 'success');
            state.selectedUnpaidTicket = null;
            await loadTickets();

        } catch (err) {
            showToast('Lỗi xử lý thanh toán: ' + err.message, 'error');
        } finally {
            btnExecutePayment.disabled = false;
            btnExecutePayment.innerHTML = `
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
                    <line x1="1" y1="10" x2="23" y2="10"></line>
                </svg>
                <span>Xác Nhận Thanh Toán</span>
            `;
        }
    }

    // ==============================================================
    // TAB 4: CUSTOMERS
    // ==============================================================
    async function loadCustomers() {
        try {
            const res = await fetch('/api/customers');
            state.customers = await res.json();
            renderCustomersTable();
            populateBookingCustomers();
        } catch (err) {
            showToast('Lỗi nạp khách hàng: ' + err.message, 'error');
        }
    }

    function renderCustomersTable() {
        customerTotalCount.textContent = `Tổng: ${state.customers.length} khách hàng`;
        customersTable.innerHTML = state.customers.map(c => {
            let policyDesc = 'Giá cơ bản +0%';
            let badgeClass = 'badge-dark';
            if (c.type === 'STUDENT') {
                policyDesc = 'Giảm 20% tổng giá vé';
                badgeClass = 'badge-primary';
            } else if (c.type === 'VIP') {
                policyDesc = 'Giảm 15% tổng giá vé';
                badgeClass = 'badge-primary';
            }

            return `
                <tr>
                    <td><strong class="text-blue">${c.id}</strong></td>
                    <td><strong>${escapeHtml(c.fullName)}</strong></td>
                    <td>${escapeHtml(c.phone)}</td>
                    <td>${escapeHtml(c.email || '(Chưa có)')}</td>
                    <td><span class="badge ${badgeClass}">${c.typeName}</span></td>
                    <td style="color: #2563EB; font-weight: 600;">${policyDesc}</td>
                </tr>
            `;
        }).join('');
    }

    function populateBookingCustomers() {
        const curVal = bookingCustSelect.value;
        bookingCustSelect.innerHTML = '<option value="">-- Vui lòng chọn khách hàng --</option>' + 
            state.customers.map(c => `<option value="${c.id}">${c.id} - ${escapeHtml(c.fullName)} (${c.typeName})</option>`).join('');
        if (curVal) bookingCustSelect.value = curVal;
    }

    async function onAddCustomer(e) {
        e.preventDefault();
        const name = document.getElementById('cust-name').value.trim();
        const phone = document.getElementById('cust-phone').value.trim();
        const email = document.getElementById('cust-email').value.trim();
        const type = document.getElementById('cust-type').value;

        if (!name || !phone) {
            showToast('Họ tên và Số điện thoại không được để trống!', 'error');
            return;
        }

        try {
            const res = await fetch('/api/customers', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, phone, email, type })
            });
            const data = await res.json();

            if (!res.ok || !data.success) {
                // BookingException: So dien thoai da ton tai
                showToast(data.error || 'Thêm khách hàng thất bại!', 'error');
                return;
            }

            showToast('Thêm khách hàng thành công!', 'success');
            addCustomerForm.reset();
            await loadCustomers();
        } catch (err) {
            showToast('Lỗi máy chủ: ' + err.message, 'error');
        }
    }

    async function onQuickAddCustomer(e) {
        e.preventDefault();
        const name = document.getElementById('quick-cust-name').value.trim();
        const phone = document.getElementById('quick-cust-phone').value.trim();
        const email = document.getElementById('quick-cust-email').value.trim();
        const type = document.getElementById('quick-cust-type').value;

        if (!name || !phone) {
            showToast('Họ tên và Số điện thoại là bắt buộc!', 'error');
            return;
        }

        try {
            const res = await fetch('/api/customers', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, phone, email, type })
            });
            const data = await res.json();

            if (!res.ok || !data.success) {
                showToast(data.error || 'Thêm khách hàng thất bại!', 'error');
                return;
            }

            showToast('Đã thêm khách hàng: ' + name, 'success');
            quickCustForm.reset();
            quickCustModal.classList.remove('active');
            await loadCustomers();

            // Auto select new customer
            if (data.customer) {
                bookingCustSelect.value = data.customer.id;
                onCustomerOrShowtimeChange();
            }
        } catch (err) {
            showToast('Lỗi máy chủ: ' + err.message, 'error');
        }
    }

    // ==============================================================
    // TAB 5: TICKETS & REVENUE
    // ==============================================================
    async function loadTickets() {
        try {
            const res = await fetch('/api/tickets');
            const data = await res.json();
            state.tickets = data.tickets || [];

            // Update 4 stats
            statTotalTickets.textContent = data.total || 0;
            statPaidTickets.textContent = data.paidCount || 0;
            statUnpaidTickets.textContent = data.unpaidCount || 0;
            statTotalRevenue.textContent = formatVND(data.totalRevenue || 0);

            renderAllTickets();
            renderUnpaidTickets();
        } catch (err) {
            showToast('Lỗi nạp danh sách vé: ' + err.message, 'error');
        }
    }

    function renderAllTickets() {
        let list = state.tickets;
        if (state.ticketFilter === 'paid') {
            list = list.filter(t => t.paid);
        } else if (state.ticketFilter === 'unpaid') {
            list = list.filter(t => !t.paid);
        }

        if (list.length === 0) {
            allTicketsTable.innerHTML = `
                <tr>
                    <td colspan="10" style="text-align: center; padding: 40px; color: #64748B;">
                        Không có vé nào trong danh mục này.
                    </td>
                </tr>
            `;
            return;
        }

        allTicketsTable.innerHTML = list.map(t => {
            const statusBadge = t.paid 
                ? `<span class="badge badge-showing">Đã TT (${escapeHtml(t.paymentMethod || 'Tiền mặt')})</span>`
                : `<span class="badge badge-stopped">Chưa thanh toán</span>`;

            const actionBtn = t.paid
                ? `<button class="btn btn-secondary btn-sm" onclick="window.viewTicketReceipt('${t.id}')">Xem vé</button>`
                : `<button class="btn btn-primary btn-sm" onclick="window.goToPayTicket('${t.id}')">Thanh toán</button>`;

            return `
                <tr>
                    <td><strong class="text-blue">${t.id}</strong></td>
                    <td><strong>${escapeHtml(t.customerName)}</strong></td>
                    <td>${escapeHtml(t.customerTypeName)}</td>
                    <td>${escapeHtml(t.movieTitle)}</td>
                    <td>${escapeHtml(t.roomName)}</td>
                    <td>${t.startTime}</td>
                    <td><span class="badge badge-dark">${t.seatNumber}</span></td>
                    <td><strong>${formatVND(t.finalPrice)}</strong></td>
                    <td>${statusBadge}</td>
                    <td>${actionBtn}</td>
                </tr>
            `;
        }).join('');
    }

    window.goToPayTicket = function(ticketId) {
        switchTab('tab-payment');
        selectUnpaidTicket(ticketId);
    };

    window.viewTicketReceipt = function(ticketId) {
        const ticket = state.tickets.find(t => t.id === ticketId);
        if (ticket) openTicketReceiptModal(ticket);
    };

    // ==============================================================
    // UTILITY HELPERS
    // ==============================================================
    function formatVND(num) {
        return new Intl.NumberFormat('vi-VN').format(Math.round(num)) + ' đ';
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        toastContainer.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.25s ease';
            setTimeout(() => toast.remove(), 250);
        }, 3500);
    }

    // Start App
    init();
});
