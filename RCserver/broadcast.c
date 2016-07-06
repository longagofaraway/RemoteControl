#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>
#include <sys/socket.h>

void die(char *msg)
{
    FILE * f = fopen("/home/debian/projects/RCserver/log","w");
    fwrite(msg,1,strlen(msg),f);
    fclose(f);
    exit(1);
}

int main()
{
    int sock;   
    int recv_len;
    int port = 5001;
    struct sockaddr_in saddr,client;
    int client_addr_len = sizeof(client); 
    char buf[512];

    if ((sock=socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
    {
        die("socket");
    }

    saddr.sin_family = AF_INET;
    saddr.sin_port = htons(port);
    saddr.sin_addr.s_addr = htonl(INADDR_ANY);

    //bind socket to port
    if(bind(sock, (struct sockaddr *) &saddr, sizeof(saddr)) < 0)
    {
        die("bind");
    }

    while(1)
    {
        if((recv_len = recvfrom(sock, buf, 512, 0, (struct sockaddr *) &client, &client_addr_len)) == -1)
        {
            die("recvfrom");
        }
        printf("Received packet from %s:%d\n", inet_ntoa(client.sin_addr), ntohs(client.sin_port));
        printf("Data: %s\n" , buf);

        client.sin_port = htons(port);

        if (sendto(sock, "abc", 3, 0, (struct sockaddr *) &client, client_addr_len) == -1)
        {
            die("sendto");
        }
    }

    close(sock);
    return 0;
}


