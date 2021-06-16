//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef WEB_FACEBOOK_SENDMESSAGE_H
#define WEB_FACEBOOK_CREATETOKEN_H

#include "FacebookServiceStates.h"

#include <QString>
#include <QVector>

//==============================================================================
// Forward Declarations
//==============================================================================

class QDomDocument;

//==============================================================================
// Declarations
//==============================================================================

/** Message for a user's Facebook page. */
typedef QString FacebookMessage;

/** Request operator. */
struct FacebookSendMessageRequest
{
  typedef FacebookMessage Input;              ///< Input type.
  enum { requiresSession = true };            ///< Authentication required flag.
  const static QString requestName;           ///< Request name.

  /**
   * Posts a message to the user's Facbook page.
   * @param input input parameters.
   * @param parameters parameters sent to the server.
   * @return status.
   */
  FacebookServiceState operator()(const Input& input, QVector<QString>& parameters);
};

/** Response operator. */
struct FacebookSendMessageResponse
{
  typedef void* Output;                       ///< Output type.

  /**
   * Parses response for message request.
   * @param document XML document that contains the response.
   * @param output parsed output.
   * @return status.
   */
  FacebookServiceState operator()(const QDomDocument& document, Output& output);
};

#endif // WEB_FACEBOOK_CREATETOKEN_H
