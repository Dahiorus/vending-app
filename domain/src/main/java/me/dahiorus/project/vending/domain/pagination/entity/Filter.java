package me.dahiorus.project.vending.domain.pagination.entity;

public record Filter<S>(S probe, FilterMatcher filterMatcher) {}
