//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_FACEBOOKSERVICEUTILS_H
#define WEB_FACEBOOK_FACEBOOKSERVICEUTILS_H

#include "FacebookServiceStates.h"

#include <QString>

//==============================================================================
// Forward Declarations
//==============================================================================

class QDomDocument;
class QNetworkReply;

//==============================================================================
// Declarations
//==============================================================================

/** Utilities for Facebook requests. */
namespace FacebookServiceUtils
{
  /**
   * Creates a signature for a Facebook request.
   * @param parameters request parameters.
   * @param secret secret.
   * @param generated signature.
   */
  QString sign(QVector<QString>& parameters, QString secret);

  /**
   * Creates data for POSTing data to the Facebook REST server.
   * @param parameters request parameters.
   * @param signature request signature.
   * @param data resulting POST data.
   */
  void createPostData(const QVector<QString>& parameters, QString signature,
    QByteArray& data);

  /**
   * Checks a network reply for Facebook's error codes.
   * @param reply network reply.
   * @param replyData the reply's data content.
   * @param document resulting XML document if the response contained a valid
   *                 XML response.
   */
  FacebookServiceState checkForError(QNetworkReply* reply,
    const QByteArray& replyData, QDomDocument& document);

  /**
   * Generates a new call ID for a request.
   * @return call ID.
   */
  int generateCallId();
}

#endif // WEB_FACEBOOK_FACEBOOKSERVICEUTILS_H
