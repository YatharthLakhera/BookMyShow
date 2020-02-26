package com.project.bookmyshow.controller;

import com.project.bookmyshow.constants.ErrorMessages;
import com.project.bookmyshow.models.APIResponse;
import com.project.bookmyshow.models.request.AddShowRequest;
import com.project.bookmyshow.models.response.ShowDetail;
import com.project.bookmyshow.models.response.ErrorResponse;
import com.project.bookmyshow.services.CustomerService;
import com.project.bookmyshow.services.ShowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/shows")
public class ShowController {

    @Autowired
    private ShowService showService;
    @Autowired
    private CustomerService customerService;

    /**
     * This API returns the list of all the live show details {@link ShowDetail}
     * that are available in the cinemas
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<ShowDetail> getLiveShowList() {
        return showService.getShowDetailsList(true);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/{showId}", method = RequestMethod.GET)
    public ShowDetail getLiveShowDetails(@PathVariable(name = "showId") int liveShowId) {
        return showService.getShowDetails(liveShowId);
    }

    /**
     * This API returns the list of all the show present in DB details {@link ShowDetail}
     * that are available in the cinemas for checking the history of the shows that
     * were available(with restriction to show for last 3 months).
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<ShowDetail> getAllShowDetails() {
        return showService.getShowDetailsList(false);
    }

    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<APIResponse> addShow(@RequestBody AddShowRequest addShowRequest) {
        if (customerService.isAdmin(addShowRequest.getCustomerId())) {
            return new ResponseEntity<>(showService.addNewShowToDB(addShowRequest), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(
                    new ErrorResponse(ErrorMessages.USER_PERMISSIONS_ISSUE),
                    HttpStatus.FORBIDDEN
            );
        }
    }
}