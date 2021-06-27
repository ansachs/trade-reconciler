# trade-reconciler

constructor takes a list of position in the following format:  
{"id": 1234, "symbol":"ACOR1 210416C00001000", "type": "short/long", "quantity": 12}

and a list of trades in the following format:  
{"id": 4569, "quantity": 5, "legs":[{"symbol": "ACOR1 210416C00001000" , "type":"short/long"}]}

returns a 'results' object containing unmatched positions and unmatched trades