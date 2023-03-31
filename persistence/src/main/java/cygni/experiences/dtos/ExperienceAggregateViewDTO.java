package cygni.experiences.dtos;

import java.util.UUID;

public record ExperienceAggregateViewDTO  (UUID id, String artist, String venue, String date, int price, int totalSeats, int bookedSeats, int availableSeats, boolean cancelled, String[] notes) {
}
