$(document).ready(function () {
    updateNavbarActiveLink();
    updateCartItemCount();

    $('#lnk-logout').click(() => {
        $('#form-logout').submit();
    });
});

function updateNavbarActiveLink() {
    $('.navbar-nav li').removeClass('active');

    if (window.location.pathname === '/' || window.location.href.endsWith('/home')) {
        $('.navbar-nav #home').addClass('active');
    } else if (window.location.href.endsWith('/beverage/crate')) {
        $('.navbar-nav #crates').addClass('active');
    } else if (window.location.href.endsWith('/beverage/bottle')) {
        $('.navbar-nav #bottles').addClass('active');
    } else if (window.location.href.endsWith('/cart')) {
        $('.navbar-nav #cart').addClass('active');
    } else if (window.location.href.endsWith('/order')) {
        $('.navbar-nav #order').addClass('active');
    } else if (window.location.href.endsWith('/customer')) {
        $('.navbar-nav #customer').addClass('active');
    }
}

function updateCartItemCount() {
    $.ajax({
        url: '/api/cart-items/count',
        type: 'GET',
        success: (data) => {
            $('#badge-cart-item-count').text(data);
            $('#txt-cart-item-count').text(data);

            $('input[name ="cartItemCount"]').val(data);
        },
        error: () => {
            $('#badge-cart-item-count').text(0);
            $('#txt-cart-item-count').text(0);

            $('input[name ="cartItemCount"]').val(0);
        }
    });
}

function updateCartTotal() {
    $.ajax({
        url: '/api/cart-items/total-price',
        type: 'GET',
        success: (data) => {
            $('#txt-cart-total').text(data.toFixed(2) + '€');
        }
    });
}

function addItemToCart(beverageId, quantity, isBottle, successCallback) {
    blockScreen();

    $.ajax({
        url: '/api/cart-items',
        type: 'POST',
        data: JSON.stringify({
            beverageId: beverageId,
            beverageType: isBottle ? 'BOTTLE' : 'CRATE',
            quantity: quantity
        }),
        contentType: 'application/json',
        success: (data) => {
            unblockScreen();
            updateCartItemCount();
            alertify.success("Item successfully added to the cart.");

            successCallback(data);
        },
        error: () => {
            unblockScreen();
            alertify.error("Error in adding item to the cart.");
        }
    });
}

function removeItemFromCart(cartItemId, successCallback) {
    blockScreen();

    $.ajax({
        url: '/api/cart-items/' + cartItemId,
        type: 'DELETE',
        success: () => {
            unblockScreen();
            successCallback();
            updateCartTotal();
            updateCartItemCount();
            alertify.success('Item successfully removed from the cart');
        },
        error: () => {
            unblockScreen();
            alertify.error("Error in removing item from the cart.");
        }
    });
}

function blockScreen() {
    $('#screen-blocker').show();
}

function unblockScreen() {
    $('#screen-blocker').hide();
}

function updateAllowedQuantity(allowedQuantity, selectQuantity, btnAddToCart) {
    selectQuantity.html('');

    if (allowedQuantity < 1) {
        btnAddToCart.attr('disabled', true);
        selectQuantity.attr('disabled', true);
    } else {
        for (let i = 1; i <= allowedQuantity; i++) {
            let optionHtml = '<option value="' + i + '">' + i + '</option>';

            selectQuantity.append(optionHtml);
        }
    }
}

function isValidPositiveInteger(value) {
    return (/^[0-9]\d*$/.test(value) && parseInt(value) > 0);
}

function addStockToBeverage(id, quantity, isBottle, successCallback) {
    blockScreen();

    let url = '/api/'
        + (isBottle ? 'bottles/' : 'crates/')
        + id
        + '/stock';

    $.ajax({
        url: url,
        type: 'PATCH',
        data: JSON.stringify({
            quantity: quantity
        }),
        contentType: 'application/json',
        success: (data) => {
            unblockScreen();
            alertify.success("Quantity successfully added.");

            successCallback(data);
        },
        error: () => {
            unblockScreen();
            alertify.error("Error in adding quantity.");
        }
    });
}

function regenerateInvoice(orderNumber) {
    blockScreen();

    $.ajax({
        url: '/api/invoice/order/' + orderNumber,
        type: 'POST',
        success: (data) => {
            unblockScreen();
            alertify.success("Invoice successfully regenerated.");
        },
        error: () => {
            unblockScreen();
            alertify.error("Error in regenerating invoice.");
        }
    });
}
