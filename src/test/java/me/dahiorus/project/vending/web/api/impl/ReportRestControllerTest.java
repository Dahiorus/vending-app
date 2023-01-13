package me.dahiorus.project.vending.web.api.impl;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.ReportDTO;
import me.dahiorus.project.vending.domain.service.impl.ReportDtoServiceImpl;
import me.dahiorus.project.vending.web.api.assembler.ReportDtoModelAssembler;

@WebMvcTest(ReportRestController.class)
class ReportRestControllerTest extends RestControllerTest
{
  @MockBean
  ReportDtoServiceImpl reportDtoService;

  @MockBean
  ReportDtoModelAssembler modelAssembler;

  @MockBean
  PagedResourcesAssembler<ReportDTO> pageModelAssembler;

  @Nested
  class ReportTests
  {
    @Captor
    ArgumentCaptor<ReportDTO> reportArg;

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanReportStock() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(reportDtoService.report(id)).then(invoc -> {
        ReportDTO report = new ReportDTO();
        report.setId(UUID.randomUUID());
        return report;
      });
      when(modelAssembler.toModel(reportArg.capture())).then(invoc -> EntityModel.of(invoc.getArgument(0)));

      mockMvc.perform(post("/api/v1/vending-machines/{id}/report", id))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/v1/reports/" + reportArg.getValue()
          .getId())))
        .andExpect(jsonPath("id").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(post("/api/v1/vending-machines/{id}/report", id))
        .andExpect(status().isForbidden());

      verify(reportDtoService, never()).report(id);
    }

    @Test
    @WithAnonymousUser
    void anonymousIsUnauthorized() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(post("/api/v1/vending-machines/{id}/report", id))
        .andExpect(status().isUnauthorized());

      verify(reportDtoService, never()).report(id);
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void entityNotFoundIsThrown() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(reportDtoService.report(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      mockMvc.perform(post("/api/v1/vending-machines/{id}/report", id))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  class ListTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanListReports() throws Exception
    {
      Pageable pageable = PageRequest.of(1, 20, Direction.DESC, "createdAt");
      PageImpl<ReportDTO> page = new PageImpl<>(List.of(new ReportDTO()), pageable, 1L);
      when(reportDtoService.list(eq(pageable), any(), any()))
        .thenReturn(page);
      when(pageModelAssembler.toModel(page, modelAssembler)).thenReturn(PagedModel.wrap(page.getContent(),
        new PageMetadata(pageable.getPageSize(), pageable.getPageNumber(), page.getTotalElements())));

      mockMvc.perform(get("/api/v1/reports").queryParam("page", "2")
        .queryParam("size", "20")
        .queryParam("stringMatcher", "EXACT"))
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      mockMvc.perform(get("/api/v1/reports").queryParam("page", "2")
        .queryParam("size", "20")
        .queryParam("stringMatcher", "EXACT"))
        .andExpect(status().isForbidden());

      verify(reportDtoService, never()).list(any(), any(), any());
    }

    @Test
    @WithAnonymousUser
    void anonymousIsUnauthorized() throws Exception
    {
      mockMvc.perform(get("/api/v1/reports").queryParam("page", "2"))
        .andExpect(status().isUnauthorized());

      verify(reportDtoService, never()).list(any(), any(), any());
    }
  }

  @Nested
  class ReadTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanGetReport() throws Exception
    {
      UUID id = UUID.randomUUID();
      ReportDTO report = new ReportDTO();
      report.setId(id);
      when(reportDtoService.read(id)).thenReturn(report);
      when(modelAssembler.toModel(report)).then(invoc -> EntityModel.of(invoc.getArgument(0)));

      mockMvc.perform(get("/api/v1/reports/{id}", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(jsonPath("id").value(id.toString()));
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(get("/api/v1/reports/{id}", id))
        .andExpect(status().isForbidden());

      verify(reportDtoService, never()).read(id);
    }

    @Test
    @WithAnonymousUser
    void anonymousIsUnauthorized() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(get("/api/v1/reports/{id}", id))
        .andExpect(status().isUnauthorized());

      verify(reportDtoService, never()).read(id);
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void getNonExistingReport() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(reportDtoService.read(id)).thenThrow(new EntityNotFound(Report.class, id));

      mockMvc.perform(get("/api/v1/reports/{id}", id))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  class DeleteTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanDeleteReport() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(delete("/api/v1/reports/{id}", id))
        .andExpect(status().isNoContent());

      verify(reportDtoService).delete(id);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(delete("/api/v1/reports/{id}", id))
        .andExpect(status().isForbidden());

      verify(reportDtoService, never()).delete(id);
    }

    @Test
    @WithAnonymousUser
    void anonymousIsUnauthorized() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(delete("/api/v1/reports/{id}", id))
        .andExpect(status().isUnauthorized());

      verify(reportDtoService, never()).delete(id);
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void deleteNonExistingReport() throws Exception
    {
      UUID id = UUID.randomUUID();
      doThrow(new EntityNotFound(Report.class, id)).when(reportDtoService)
        .delete(id);

      mockMvc.perform(delete("/api/v1/reports/{id}", id))
        .andExpect(status().isNoContent());
    }
  }
}
