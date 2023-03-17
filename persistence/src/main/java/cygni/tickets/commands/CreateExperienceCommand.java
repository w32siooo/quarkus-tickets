package cygni.tickets.commands;

public record CreateExperienceCommand(String artist, String venue, String date, int price, int seats) {
}
