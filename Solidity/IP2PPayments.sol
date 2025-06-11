////Define la interfaz del contrato para que las aplicaciones externas sepan qué funciones pueden llamar/////

// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

interface IP2PPayments {
    // Eventos
    event PaymentInitiated(
        bytes32 indexed paymentId,
        address indexed sender,
        address indexed receiver,
        uint256 amount,
        bytes32 messageHash,
        uint256 deadline
    );
    event PaymentConfirmed(
        bytes32 indexed paymentId,
        address indexed receiver
    );
    event PaymentCancelled(
        bytes32 indexed paymentId,
        address indexed sender
    );
    event DisputeOpened(
        bytes32 indexed paymentId,
        address indexed disputer
    );
    event FundsReleasedFromDispute(
        bytes32 indexed paymentId,
        address indexed winner,
        uint256 amount
    );

    // Funciones
    function initiatePayment(
        address _receiver,
        uint256 _amount,
        bytes32 _messageHash,
        uint256 _deadline // Unix timestamp
    ) external payable returns (bytes32 paymentId);

    function confirmPayment(bytes32 _paymentId) external;

    function cancelPayment(bytes32 _paymentId) external;

    function openDispute(bytes32 _paymentId) external;

    function releaseFundsFromDispute(
        bytes32 _paymentId,
        address _winner
    ) external;

    function getPaymentDetails(
        bytes32 _paymentId
    )
        external
        view
        returns (
            address sender,
            address receiver,
            uint256 amount,
            bytes32 messageHash,
            uint256 deadline,
            uint256 timestamp,
            string memory status
        );

    function getUserPayments(
        address _user
    ) external view returns (bytes32[] memory);

}
