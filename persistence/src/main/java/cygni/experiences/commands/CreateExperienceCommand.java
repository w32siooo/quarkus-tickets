package cygni.experiences.commands;

public record CreateExperienceCommand(String artist, String venue, String date, int price, int seats) {
}
