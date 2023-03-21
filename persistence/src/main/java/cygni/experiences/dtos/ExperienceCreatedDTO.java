package cygni.experiences.dtos;

public record ExperienceCreatedDTO (String artist, String venue,
                                    String date, int price, int seats, String aggregateID) {
}
