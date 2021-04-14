package com.drift.interview.reporting;

import com.drift.interview.model.Conversation;
import com.drift.interview.model.ConversationResponseMetric;
import com.drift.interview.model.Message;

import java.sql.SQLOutput;
import java.util.List;

public class ConversationMetricsCalculator {
  public ConversationMetricsCalculator() {}

  /**
   *          Implemented by Alejandro Brito on 4/12/21
   * Returns a ConversationResponseMetric object which can be used to power data visualizations on the front end.
   *
   * So far, we are calculating two metrics: averageResponseTime & inquiryToResponseRatio
   *  1. averageResponseTime: returns average of response times between end user and team member in this Conversation. If
   *  end user sends messages in a sequence, only calculate response time from their first message.
   *
   *  2. inquiryToResponseRatio: calculates the ratio of questions an end user has to how many responses a Team Member has.
   *  We consider a message a question if the message the end user sent ends with a '?'. This can be insightful for the following:
   *    -if ratio > 1, it means that the user had many questions. If this number is very high, maybe the website does not
   *    answer many of the questions they have or the team member is not answering questions effectively
   *    -if ratio = 1, it means that user had an even amount of questions to total responses a team member provides. This
   *    should not be a cause for concern
   *    -if ratio < 1, it means that team member provided more responses than the end user had questions. This also shouldn't
   *    be a cause for concern
   *    -if ratio = 0, it means end user had 0 inquiries. Remember we count inquiries as end user messages ending with '?'
   *
   *    NOTE: the inquiryRatio has some flaws. End Users might not always end their questions with a '?'. A Team Member's
   *    response counts as 1 (even if they provide more than 1 message) which may produce a high ratio even though all the
   *    questions are being answered. This ratio is not definitive: it is a tool to provide insight to management about how
   *    certain conversations went and if maybe the website's FAQs could be improved to include questions that keep being asked
   *
   */
  ConversationResponseMetric calculateAverageResponseTime(Conversation conversation) {
    List<Message> messages = conversation.getMessages();

    // normally, we would check conversation object for nullness. However, the Conversation builder assures that the
    // object being passed is not null

    double averageResponseTime = 0;
    double inquiryToResponseRatio = 0;
    double endUserTime = 0;
    double teamMemberTime = 0;
    int totalConversations = 0;

    for(int i = 0; i < messages.size(); i++) {
      // check if current message is from an end user. If so, start tracking how long until a team member writes a response
      if(!messages.get(i).isTeamMember()) {
        // get the time of the FIRST message the end user sent, we don't care about consecutive messages
        // they sent until a team member responds
        endUserTime = messages.get(i).getCreatedAt();

        // check when is the next time a team member sends a message, starting from the first message an end user sends.
        for(int j = i; j < messages.size(); j++) {
          if(messages.get(j).isTeamMember()) {
            teamMemberTime = messages.get(j).getCreatedAt();
            // change index location (i) of 1st for loop to j (which holds the location of when a team member responds).
            // That way we don't need to iterate over messages twice
            i = j;
            break;
          }
          // check if the message end user sent ends in a question mark. If so, we can assume it is an inquiry
          else {
            String userMessage = messages.get(j).getMessage().trim();
            if(userMessage.endsWith("?")) {
              inquiryToResponseRatio++;
            }
          }
        }
        // calculate response time by subtracting the time creation of 1st end user message from the team member's response.
        // add it to any previous conversations we had as well
        averageResponseTime = averageResponseTime + (teamMemberTime - endUserTime);
        totalConversations++;
      }
    }
      // calculate total average response time by adding the response time of all the conversations and dividing by how many
      // total conversations there was. If team member never responded in this chat, we can expect this to be a negative number
      averageResponseTime /=  totalConversations;
      // calculate inquiryToResponseRatio by dividing total number of inquiry messages by team member responses
      inquiryToResponseRatio /= totalConversations;

    // At some point, this inquiryToResponseRatio should be implemented correctly and returned in the ConversationResponseMetric
    // JSON. For now, we will just print it to the console
    System.out.println("inquiryToResponseRatio for conversation " + conversation.getId() + ": " + inquiryToResponseRatio);

    return ConversationResponseMetric.builder()
        .setConversationId(conversation.getId())
        .setAverageResponseMs(averageResponseTime)
        .build();
  }
}
