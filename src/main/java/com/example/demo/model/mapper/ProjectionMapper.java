package com.example.demo.model.mapper;

import com.example.demo.PojaGenerated;
import com.example.demo.entity.Projection;
import com.example.demo.model.ProjectionModel;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@PojaGenerated
@Component
public class ProjectionMapper implements Function<Projection, ProjectionModel> {

  @Override
  public ProjectionModel apply(Projection projection) {
    return new ProjectionModel(
        projection.getId(),
        projection.getDateTime(),
        projection.getSeatPrice(),
        projection.getMovie().getId(),
        projection.getRoom().getId());
  }
}
