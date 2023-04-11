package cygni.users.commands;

import java.util.UUID;

public record BuyTicketCommand(UUID experienceID, Integer seats) {}
