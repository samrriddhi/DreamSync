package com.dreamsync.mapper;

import com.dreamsync.dto.TaskSummaryDTO;
import com.dreamsync.entity.Task;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskMapper {

    public TaskSummaryDTO toSummaryDTO(Task task) {

        TaskSummaryDTO dto = new TaskSummaryDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());

        return dto;
    }

    public List<TaskSummaryDTO> toSummaryDTOList(List<Task> tasks) {

        List<TaskSummaryDTO> dtoList = new ArrayList<>();

        for (Task task : tasks) {
            dtoList.add(toSummaryDTO(task));
        }

        return dtoList;
    }
}